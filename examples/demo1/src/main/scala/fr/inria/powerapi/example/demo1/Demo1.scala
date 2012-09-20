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
package fr.inria.powerapi.example.demo1
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.listener.cpudisk.jfreechart.CpuDiskListener
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.sensor.disk.proc.DiskSensor
import fr.inria.powerapi.formula.disk.single.DiskFormula
import fr.inria.powerapi.core.Process
import scalax.io.Resource
import akka.util.duration._
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import javax.swing.SwingUtilities
import akka.util.Duration

class Demo1Listener extends CpuDiskListener {
  override def preStart() {
    cleanupSchedule = context.system.scheduler.schedule(Duration.Zero, refreshRate) {
      cleanupByMin()
    }
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        Chart.run()
      }
    })
  }

  override def postStop() {
    cleanupSchedule.cancel()
  }

  override def display(timestamp: Long) {
    val agg = aggregate(timestamp)
    if (justTotal) {
      Chart.process(Map("total" -> agg.foldLeft(0: Double)((acc, entry) => acc + entry._2)), timestamp)
    } else {
      Chart.process(agg, timestamp)
    }
  }
}

object Demo1 extends App {
  Array(
    classOf[CpuSensor],
    classOf[CpuFormula],
    classOf[DiskSensor],
    classOf[DiskFormula]).foreach(PowerAPI.startEnergyModule(_))

  def getPids = {
    val PSFormat = """^\s*(\d+).*""".r
    val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case PSFormat(id) => id.toInt
          case _ => 1
        }
    })
    pids - pids.max
  }

  val pids = scala.collection.mutable.Set[Int]()
  val dur = 1 second
  def udpateMonitoredPids() {
    val currentPids = scala.collection.mutable.Set[Int](getPids: _*)

    val oldPids = pids -- currentPids
    oldPids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = dur))
    pids --= oldPids

    val newPids = currentPids -- pids
    newPids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = dur))
    pids ++= newPids
  }

  PowerAPI.startMonitoring(listenerType = classOf[Demo1Listener])

  val startingTime = System.currentTimeMillis
  while (System.currentTimeMillis - startingTime < (2 hours).toMillis) {
    udpateMonitoredPids()
    Thread.sleep((250 milliseconds).toMillis)
  }

  PowerAPI.stopMonitoring(listenerType = classOf[Demo1Listener])

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula],
    classOf[DiskSensor],
    classOf[DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
}