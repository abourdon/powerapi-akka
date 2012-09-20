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
package fr.inria.powerapi.listener.cpudisk.jfreechart
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Process
import scalaz._
import Scalaz._
import akka.util.duration._
import akka.util.Duration
import akka.actor.Cancellable
import javax.swing.SwingUtilities

trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val refreshRate = load(conf => Duration.parse(conf.getString("powerapi.listener-cpudisk-jfreechart.refresh-rate")))(1 second)
  lazy val aggregateByDevice = load(_.getBoolean("powerapi.listener-cpudisk-jfreechart.aggregate-by-device"))(true)
  lazy val justTotal = load(_.getBoolean("powerapi.listener-cpudisk-jfreechart.just-total"))(false)
}

class CpuDiskListener extends Listener with Configuration {
  // cache = Map(timestamp -> Map(process -> Map(device name -> power value)))
  lazy val cache = new collection.mutable.HashMap[Long, Map[Process, Map[String, Double]]]()

  var cleanupSchedule: Cancellable = _

  def messagesToListen = Array(classOf[CpuFormulaValues], classOf[DiskFormulaValues])

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

  def acquire = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
    case diskFormulaValues: DiskFormulaValues => process(diskFormulaValues)
  }

  def process(cpuFormulaValues: CpuFormulaValues) {
    addEntry(cpuFormulaValues.tick.timestamp, cpuFormulaValues.tick.subscription.process, "cpu", cpuFormulaValues.energy.power)
  }

  def process(diskFormulaValues: DiskFormulaValues) {
    addEntry(diskFormulaValues.tick.timestamp, diskFormulaValues.tick.subscription.process, "disk", diskFormulaValues.energy.power)
  }

  def addEntry(timestamp: Long, process: Process, device: String, power: Double) = synchronized {
    val processes = cache.getOrElse(timestamp, Map[Process, Map[String, Double]]())
    val devices = processes.getOrElse(process, Map[String, Double]())
    cache += timestamp -> (processes + (process -> (devices + (device -> power))))
  }

  def aggregate(timestamp: Long) = cache.getOrElse(timestamp, Map[Process, Map[String, Double]]()).foldLeft(Map[String, Double]()) { (acc, process) => acc |+| process._2 }

  def clean(timestamp: Long) {
    cache -= timestamp
  }

  def display(timestamp: Long) {
    val agg = aggregate(timestamp)
    if (justTotal) {
      Chart.process(Map("total" -> agg.foldLeft(0: Double)((acc, entry) => acc + entry._2)), timestamp)
    } else {
      Chart.process(agg, timestamp)
    }
  }

  val processedTimestamps = collection.mutable.ListBuffer[Long]()
  def cleanupByMin(): Unit = synchronized {
    if (cache.size > 1) {
      val first = cache.minBy(_._1)
      if (processedTimestamps.contains(first._1)) {
        log.error("already processed timestamp " + first._1)
        clean(first._1)
        cleanupByMin()
      } else {
        display(first._1)
        clean(first._1)
        processedTimestamps += first._1
      }
    }
  }

  def cleanupByTwoMin() = synchronized {
    if (cache.size > 1) {
      val first = cache.minBy(_._1)
      val second = (cache - first._1).minBy(_._1)
      if (second._2.size >= first._2.size) {
        if ((first._2.foldLeft(Set[(Process, Set[String])]()) { (acc, process) => acc + ((process._1, process._2.keySet)) } &~ second._2.foldLeft(Set[(Process, Set[String])]()) { (acc, process) => acc + ((process._1, process._2.keySet)) }).isEmpty) {
          display(first._1)
          clean(first._1)
        }
      }
    }
  }
}