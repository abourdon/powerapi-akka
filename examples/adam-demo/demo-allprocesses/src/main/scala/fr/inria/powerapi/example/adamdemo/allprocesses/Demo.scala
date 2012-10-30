/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PowerAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.example.adamdemo.allprocesses

import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.core.Process
import scalax.io.Resource
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import akka.util.duration._
import java.lang.management.ManagementFactory
import scalax.file.Path
import scalax.io.StandardOpenOption.WriteTruncate
import java.util.Timer
import java.util.TimerTask

class DemoListener extends fr.inria.powerapi.example.adamdemo.full.DemoListener {
  override def init() {}

  override def display(timestamp: Long) {
    println(cache(timestamp)(-1)("cpu"))
  }
}

object Demo extends App {
  Path.fromString("/tmp/powerapi.demo-allprocesses.pid").outputStream(WriteTruncate: _*).write(ManagementFactory.getRuntimeMXBean.getName.split("@")(0))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))

  val pids = scala.collection.mutable.Set[Int]()
  val timer = new Timer()

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

  PowerAPI.startMonitoring(listenerType = classOf[DemoListener])
  timer.schedule(new TimerTask() {
    def run() {
      update()
    }
  }, 0, (250 milliseconds).toMillis)

  Thread.sleep((2 hours).toMillis)

  timer.cancel()
  PowerAPI.stopMonitoring(listenerType = classOf[DemoListener])

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))

}