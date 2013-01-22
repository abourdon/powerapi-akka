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
package fr.inria.powerapi.example.adamdemo.oneprocess

import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.core.Process
import scalax.io.Resource
import fr.inria.powerapi.library.PowerAPI
import akka.util.duration._
import java.lang.management.ManagementFactory
import scalax.file.Path
import scalax.io.StandardOpenOption.WriteTruncate
import fr.inria.powerapi.formula.cpu.max.CpuFormula

class CpuListener extends fr.inria.powerapi.listener.cpu.console.CpuListener {
  override def process(cpuFormulaMessage: CpuFormulaMessage) {
    println(cpuFormulaMessage.energy.power)
  }
}

object Demo extends App {
  Path.fromString("/tmp/powerapi.demo-oneprocess.pid").outputStream(WriteTruncate: _*).write(ManagementFactory.getRuntimeMXBean.getName.split("@")(0))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))

  val PSFormat = """^\s*(\d+).*""".r
  val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-C", "firefox", "ho", "pid")).getInputStream).lines().toList.map({
    pid =>
      pid match {
        case PSFormat(id) => id.toInt
        case _ => 1
      }
  })

  pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 1 second, listener = classOf[CpuListener]))
  Thread.sleep((2 hours).toMillis)
  pids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 1 second, listener = classOf[CpuListener]))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))

}