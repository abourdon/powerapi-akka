/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
package powerapi.example.cpumonitor
import powerapi.powerapi.PowerAPI
import powerapi.core.Clock
import java.lang.management.ManagementFactory
import powerapi.sensor.cpusensor.linux.CpuSensor
import powerapi.formula.cpuformula.simple.CpuFormula
import powerapi.core.Process
import akka.util.duration._
import powerapi.listener.cpulistener.jfreechart.CpuListener
import scala.io.Source
import powerapi.listener.cpulistener.jfreechart.CpuListener
import powerapi.formula.cpuformula.CpuFormulaValues
import powerapi.listener.cpulistener.jfreechart.Chart

class CpuMonitor extends CpuListener {
  val powers = collection.mutable.HashMap[Process, Double]()

  override def listen = {
    case cpuFormulaValues: CpuFormulaValues => {
      val process = cpuFormulaValues.tick.subscription.process
      val old = powers getOrElse (process, Double.PositiveInfinity)
      val now = cpuFormulaValues.energy.power
      // We only process the chart if the difference between old and new value are over 10%
      if (math.min(now, old) / math.max(now, old) < 0.1) {
        powers += (process -> now)
        Chart.process(cpuFormulaValues)
      }
    }
  }
}

object CpuMonitor {
  def perso {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))

    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
    PowerAPI.startMonitoring(Process(16617), 500 milliseconds, classOf[CpuListener])
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
    PowerAPI.stopMonitoring(Process(16617), 500 milliseconds, classOf[CpuListener])

    PowerAPI.stopModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }

  def current {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))

    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])

    PowerAPI.stopModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }

  def intensive {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))

    val PSFormat = """^\s*(\d+).*""".r
    val pids = Source.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).getLines.toList.map({ pid =>
      pid match {
        case PSFormat(pid) => pid.toInt
        case _ => 1
      }
    })
    pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 500 milliseconds, classOf[CpuMonitor]))
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[CpuMonitor]))

    PowerAPI.stopModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }

  def main(args: Array[String]) {
    intensive
  }
}
