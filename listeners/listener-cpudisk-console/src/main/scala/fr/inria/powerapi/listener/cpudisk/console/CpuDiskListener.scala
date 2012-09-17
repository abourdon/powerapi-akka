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
package fr.inria.powerapi.listener.cpudisk.console
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Process
import scalaz._
import Scalaz._

class CpuDiskListener extends Listener {
  // cache = Map(timestamp -> Map(process -> Map(device name -> power value)))
  val cache = new collection.mutable.HashMap[Long, Map[Process, Map[String, Double]]]()

  def messagesToListen = Array(classOf[CpuFormulaValues], classOf[DiskFormulaValues])

  def acquire = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
    case diskFormulaValues: DiskFormulaValues => process(diskFormulaValues)
  }

  def process(cpuFormulaValues: CpuFormulaValues) {
    addEntry(cpuFormulaValues.tick.timestamp, cpuFormulaValues.tick.subscription.process, "cpu", cpuFormulaValues.energy.power)
    cleanup()
  }

  def process(diskFormulaValues: DiskFormulaValues) {
    addEntry(diskFormulaValues.tick.timestamp, diskFormulaValues.tick.subscription.process, "disk", diskFormulaValues.energy.power)
    cleanup()
  }

  def addEntry(timestamp: Long, process: Process, device: String, power: Double) {
    val processes = cache.getOrElse(timestamp, Map[Process, Map[String, Double]]())
    val devices = processes.getOrElse(process, Map[String, Double]())
    cache += timestamp -> (processes + (process -> (devices + (device -> power))))
  }

  def display(timestamp: Long) {
    val aggregate = cache.getOrElse(timestamp, Map[Process, Map[String, Double]]()).foldLeft(Map[String, Double]()) { (acc, process) => acc |+| process._2 }
    aggregate.foreach(agg => print(agg._1 + " = " + agg._2 + "W, "))
    println("sum = " + aggregate.foldLeft(0: Double) { (acc, agg) => acc + agg._2 } + "W.")
  }

  def cleanup() {
    if (cache.size > 1) {
      val first = cache.minBy(_._1)
      val second = (cache - first._1).minBy(_._1)
      if (second._2.size >= first._2.size) {
        if ((first._2.foldLeft(Set[(Process, Set[String])]()) { (acc, process) => acc + ((process._1, process._2.keySet)) } &~ second._2.foldLeft(Set[(Process, Set[String])]()) { (acc, process) => acc + ((process._1, process._2.keySet)) }).isEmpty) {
          display(first._1)
          cache -= first._1
        }
      }
    }
  }
}