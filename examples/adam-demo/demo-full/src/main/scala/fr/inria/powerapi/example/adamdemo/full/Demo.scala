/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.example.adamdemo.full

import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.sensor.disk.proc.DiskSensor
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.formula.disk.single.DiskFormula
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Process
import java.lang.management.ManagementFactory
import scalax.io.Resource
import akka.util.duration._
import akka.actor.Cancellable
import akka.actor.ActorSystem
import java.util.Timer
import java.util.TimerTask

trait Scenario {
  def name(): String
  def init: Boolean
  def start(): Unit
  def stop(): Unit
}

class OneProcessScenario extends Scenario {
  lazy val psFormat = """^\s*(\d+).*""".r
  private var pids: List[Int] = _
  private var process = "firefox"

  def name = "Power consumption of an application"

  def init = {
    Chart.setTitle(name)
    DemoListener.justTotal = true
    true
  }

  def start() {
    pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-C", process, "ho", "pid")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case psFormat(id) => id.toInt
          case _ => 1
        }
    })
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 1 second))
  }

  def stop() {
    pids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 1 second))
  }

  def setProcess(process: String) {
    stop()
    this.process = process
    start()
  }
}

class OverheadWithOneProcessScenario extends Scenario {
  var externalPid: Int = _
  var externalProcess: java.lang.Process = _

  def name = "Power consumption of PowerAPI running one process"

  def init = {
    externalProcess = Runtime.getRuntime.exec(Array("/usr/bin/xterm", sys.props("user.home") + "/bin/demo-oneprocess"))
    Thread.sleep((10 seconds).toMillis)

    Chart.setTitle(name)
    DemoListener.justTotal = true
    true
  }

  def start() {
    externalPid = Resource.fromFile("/tmp/powerapi.demo-oneprocess.pid").lines().mkString.toInt

    PowerAPI.startMonitoring(process = Process(externalPid), duration = 1 second)
  }

  def stop() {
    PowerAPI.stopMonitoring(process = Process(externalPid), duration = 1 second)
    externalProcess.destroy()
  }
}

class GranularityScenario extends Scenario {
  lazy val psFormat = """^\s*(\d+).*""".r
  private var pids: List[Int] = _
  private var process = "firefox"

  def name = "Power consumption of an application by hardware devices"

  def init = {
    Chart.setTitle(name)
    DemoListener.justTotal = false
    true
  }

  def start() {
    pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-C", process, "ho", "pid")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case psFormat(id) => id.toInt
          case _ => 1
        }
    })
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 1 second))
  }

  def stop() {
    pids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 1 second))
  }

  def setProcess(process: String) {
    stop()
    this.process = process
    start()
  }
}

class AllProcessesScenario extends Scenario {
  val pids = scala.collection.mutable.Set[Int]()
  val timer = new Timer()

  def name = "Power consumption of all processes"

  def getPids = {
    val PSFormat = """^\s*(\d+).*""".r
    Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case PSFormat(id) => id.toInt
          case _ => 1
        }
    })
  }

  def update() {
    val currentPids = scala.collection.mutable.Set[Int](getPids: _*)

    val oldPids = pids -- currentPids
    oldPids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 1 second))
    pids --= oldPids

    val newPids = currentPids -- pids
    newPids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 1 second))
    pids ++= newPids
  }

  def init = {
    Chart.setTitle(name)
    DemoListener.justTotal = true
    true
  }

  def start() {
    timer.schedule(new TimerTask() {
      def run() {
        update()
      }
    }, 0, (250 milliseconds).toMillis)
  }

  def stop() {
    timer.cancel()
    pids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 1 second))
  }
}

class OverheadWithAllProcessesScenario extends Scenario {
  var externalPid: Int = _
  var externalProcess: java.lang.Process = _

  def name = "Power consumption of PowerAPI running all processes"

  def init = {
    externalProcess = Runtime.getRuntime.exec(Array("/usr/bin/xterm", sys.props("user.home") + "/bin/demo-allprocesses"))
    Thread.sleep((10 seconds).toMillis)

    Chart.setTitle(name)
    DemoListener.justTotal = true
    true
  }

  def start() {
    externalPid = Resource.fromFile("/tmp/powerapi.demo-allprocesses.pid").lines().mkString.toInt

    PowerAPI.startMonitoring(process = Process(externalPid), duration = 1 second)
  }

  def stop() {
    PowerAPI.stopMonitoring(process = Process(externalPid), duration = 1 second)
    externalProcess.destroy()
  }
}

object Demo {
  lazy val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
  lazy val demo1 = new OneProcessScenario
  lazy val demo2 = new OverheadWithOneProcessScenario
  lazy val demo3 = new GranularityScenario
  lazy val demo4 = new AllProcessesScenario
  lazy val demo5 = new OverheadWithAllProcessesScenario

  Runtime.getRuntime().exec("firefox")
  Array(classOf[CpuSensor], classOf[CpuFormula], classOf[DiskSensor], classOf[DiskFormula]).foreach(PowerAPI.startEnergyModule(_))
  PowerAPI.startMonitoring(listenerType = classOf[DemoListener])

  Thread.sleep((5 seconds).toMillis)

  def setOffset(offset: Double) {
    Chart.setOffset(offset)
  }
}