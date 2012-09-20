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
package fr.inria.powerapi.example.demo3
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.sensor.disk.proc.DiskSensor
import fr.inria.powerapi.formula.disk.single.DiskFormula
import fr.inria.powerapi.listener.cpudisk.jfreechart.CpuDiskListener
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.core.Process
import akka.util.duration._
import scalax.io.Resource

class Demo3Listener extends CpuDiskListener {
  override def display(timestamp: Long) {
    val agg = aggregate(timestamp)
    if (justTotal) {
      Chart.process(Map("total" -> agg.foldLeft(0: Double)((acc, entry) => acc + entry._2)), timestamp)
    } else {
      Chart.process(agg, timestamp)
    }
  }
}

object Demo3 extends App {
  Array(
    classOf[CpuSensor],
    classOf[CpuFormula],
    classOf[DiskSensor],
    classOf[DiskFormula]).foreach(PowerAPI.startEnergyModule(_))

  val PSFormat = """^\s*(\d+).*""".r
  Runtime.getRuntime.exec(Array("stress", "-d", "1"))
  val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-C", "stress", "ho", "pid")).getInputStream).lines().toList.map({
    pid =>
      pid match {
        case PSFormat(id) => id.toInt
        case _ => 1
      }
  })

  pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 1 second, classOf[Demo3Listener]))
  Thread.sleep((2 hours).toMillis)
  pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 1 second, classOf[Demo3Listener]))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula],
    classOf[DiskSensor],
    classOf[DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
}