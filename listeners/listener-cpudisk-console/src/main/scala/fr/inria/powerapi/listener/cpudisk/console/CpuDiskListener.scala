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

case class CpuDiskValues(
  var cpuFormulaValues: Option[CpuFormulaValues],
  var diskFormulaValues: Option[DiskFormulaValues]) {
  def isDefined = cpuFormulaValues.isDefined && diskFormulaValues.isDefined
  override def toString() = {
    if (isDefined) {
      "cpu = " + cpuFormulaValues.get.energy.power + "W, " +
        "disk = " + diskFormulaValues.get.energy.power + "W, " +
        "sum = " + (cpuFormulaValues.get.energy.power + diskFormulaValues.get.energy.power) + "W"
    } else {
      "values not correctly defined"
    }
  }
}

class CpuDiskListener extends Listener {
  val cache = new java.util.TreeMap[Long, CpuDiskValues]()

  def messagesToListen = Array(classOf[CpuFormulaValues], classOf[DiskFormulaValues])

  def acquire = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
    case diskFormulaValues: DiskFormulaValues => process(diskFormulaValues)
  }

  def process(cpuFormulaValues: CpuFormulaValues) {
    val timestamp = cpuFormulaValues.tick.timestamp
    val cacheEntry = cache.get(timestamp)
    if (cacheEntry == null) {
      cache.put(timestamp, CpuDiskValues(Some(cpuFormulaValues), None))
    } else {
      cacheEntry.cpuFormulaValues = Some(cpuFormulaValues)
      cache.put(timestamp, cacheEntry)
      aggregate(timestamp)
    }
  }

  def process(diskFormulaValues: DiskFormulaValues) {
    val timestamp = diskFormulaValues.tick.timestamp
    val cacheEntry = cache.get(timestamp)
    if (cacheEntry == null) {
      cache.put(timestamp, CpuDiskValues(None, Some(diskFormulaValues)))
    } else {
      cacheEntry.diskFormulaValues = Some(diskFormulaValues)
      cache.put(timestamp, cacheEntry)
      aggregate(timestamp)
    }
  }

  def aggregate(timestamp: Long) {
    display(timestamp)
    clearCacheEntry(timestamp)
  }

  def display(timestamp: Long) {
    val cacheEntry = cache.firstEntry()
    println(cacheEntry.getValue())
  }

  def clearCacheEntry(timestamp: Long) {
    cache.remove(timestamp)
  }
}