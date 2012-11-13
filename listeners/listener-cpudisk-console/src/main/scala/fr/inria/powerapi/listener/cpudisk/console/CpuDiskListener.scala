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
package fr.inria.powerapi.listener.cpudisk.console
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.formula.disk.api.DiskFormulaMessage
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Process
import scalaz._
import Scalaz._
import akka.util.duration._
import akka.util.Duration
import akka.actor.Cancellable

/**
 * CpuDiskListener's configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * Result display refresh rate. 1 second by default.
   */
  lazy val refreshRate = load(conf => Duration.parse(conf.getString("powerapi.listener-cpudisk-console.refresh-rate")))(1 second)
}

/**
 * CPU and disk listener, displaying result into the console from both CpuFormulaMessage and DiskFormulaMessage messages.
 *
 * Each CPU or disk result is cached into a data structure, in order to display a global result.
 *
 * @author abourdon
 */
class CpuDiskListener extends Listener with Configuration {
  // cache = Map(timestamp -> Map(process -> Map(device name -> power value)))
  lazy val cache = new collection.mutable.HashMap[Long, Map[Process, Map[String, Double]]]()

  var cleanupSchedule: Cancellable = _

  def messagesToListen = Array(classOf[CpuFormulaMessage], classOf[DiskFormulaMessage])

  override def preStart() {
    cleanupSchedule = context.system.scheduler.schedule(Duration.Zero, refreshRate) {
      cleanupByMin()
    }
  }

  override def postStop() {
    cleanupSchedule.cancel()
  }

  def acquire = {
    case cpuFormulaMessage: CpuFormulaMessage => process(cpuFormulaMessage)
    case diskFormulaMessage: DiskFormulaMessage => process(diskFormulaMessage)
  }

  def process(cpuFormulaMessage: CpuFormulaMessage) {
    addEntry(cpuFormulaMessage.tick.timestamp, cpuFormulaMessage.tick.subscription.process, "cpu", cpuFormulaMessage.energy.power)
  }

  def process(diskFormulaMessage: DiskFormulaMessage) {
    addEntry(diskFormulaMessage.tick.timestamp, diskFormulaMessage.tick.subscription.process, "disk", diskFormulaMessage.energy.power)
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
    agg.foreach(device => print(device._1 + " = " + device._2 + "W, "))
    println("sum = " + agg.foldLeft(0: Double) { (acc, device) => acc + device._2 } + "W.")
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