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
package fr.inria.powerapi.example.demo2
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
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.sensor.cpu.api.CpuSensorValues
import fr.inria.powerapi.listener.cpudisk.jfreechart.Chart
import fr.inria.powerapi.core.TickSubscription
import scalax.io.Resource

class Demo2Listener extends CpuDiskListener {
  lazy val cpuUsageCache = collection.mutable.HashMap[TickSubscription, CpuSensorValues]()

  override def messagesToListen = Array(classOf[CpuSensorValues], classOf[CpuFormulaValues], classOf[DiskFormulaValues])

  override def acquire = {
    case cpuSensorValues: CpuSensorValues => process(cpuSensorValues)
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
    case diskFormulaValues: DiskFormulaValues => process(diskFormulaValues)
  }

  def process(now: CpuSensorValues) {

    def usage(old: CpuSensorValues, now: CpuSensorValues) = {
      val processUsage = (now.processElapsedTime.time - old.processElapsedTime.time).toDouble
      val globalUsage = (now.globalElapsedTime.time - old.globalElapsedTime.time).toDouble
      if (globalUsage == 0) {
        0.0
      } else {
        math.max(0.0, processUsage / globalUsage)
      }
    }

    Chart.process(Map("cpu usage" -> usage(cpuUsageCache getOrElse (now.tick.subscription, now), now)), now.tick.timestamp)
    cpuUsageCache += (now.tick.subscription -> now)
  }
}

object Demo2 extends App {
  lazy val conf = ConfigFactory.load
  lazy val pids = JavaConversions.asScalaBuffer(conf.getIntList("powerapi.pids")).toList

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula],
    classOf[DiskSensor],
    classOf[DiskFormula]).foreach(PowerAPI.startEnergyModule(_))

  pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 500 milliseconds, classOf[Demo2Listener]))
  Thread.sleep((2 hours).toMillis)
  pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[Demo2Listener]))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula],
    classOf[DiskSensor],
    classOf[DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
}