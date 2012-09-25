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
package fr.inria.powerapi.example.adamdemo.full

import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.core.Listener
import javax.swing.SwingUtilities

object DemoListener {
  var justTotal = true
}

class DemoListener extends Listener {
  val cache = collection.mutable.Map[Long, Map[String, Double]]()

  def messagesToListen = Array(classOf[CpuFormulaValues], classOf[DiskFormulaValues])

  init()
  
  def init() {
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        Chart.run()
      }
    })
  }

  def acquire = {
    case cpuFormulaValues: CpuFormulaValues => process("cpu", cpuFormulaValues.energy.power, cpuFormulaValues.tick.timestamp)
    case diskFormulaValues: DiskFormulaValues => process("disk", diskFormulaValues.energy.power, diskFormulaValues.tick.timestamp)
  }

  def process(device: String, power: Double, timestamp: Long) {
    add(device, power, timestamp)
    flush(timestamp)
  }

  def add(device: String, power: Double, timestamp: Long) {
    val devices = cache.getOrElse(timestamp, Map[String, Double]())
    cache += timestamp -> (devices + (device -> (devices.getOrElse(device, 0: Double) + power)))
  }

  def flush(limit: Long) {
    cache.filter(entry => (entry._1 < limit)).foreach(value => {
      display(value._1)
      remove(value._1)
    })
  }

  def display(timestamp: Long) {
    if (DemoListener.justTotal) {
      Chart.add(Map("total" -> cache(timestamp).foldLeft(0: Double) { (acc, device) => acc + device._2 }), timestamp)
    } else {
      Chart.add(Map("cpu" -> cache(timestamp)("cpu")), timestamp)
      Chart.add(Map("disk" -> cache(timestamp)("disk")), timestamp)
    }
  }

  def remove(timestamp: Long) {
    cache -= timestamp
  }

}