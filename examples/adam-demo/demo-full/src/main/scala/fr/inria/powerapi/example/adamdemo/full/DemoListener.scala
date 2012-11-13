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
package fr.inria.powerapi.example.adamdemo.full

import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.formula.disk.api.DiskFormulaMessage
import fr.inria.powerapi.core.Listener
import javax.swing.SwingUtilities

object DemoListener {
  val pidNames = collection.mutable.Map[Int, String](-1 -> "all processes")
  private var justTotalRequest = true
  private var clearRequest = false

  def isNamed(pid: Int) = synchronized {
    pidNames.contains(pid)
  }

  def pidName(pid: Int, name: String) = synchronized {
    pidNames += pid -> name
  }

  def justTotal() = synchronized {
    justTotalRequest = true
  }

  def unJustTotal() = synchronized {
    justTotalRequest = false
  }

  def hasToJustTotal = synchronized {
    justTotalRequest
  }

  def clear() = synchronized {
    clearRequest = true
  }

  def unClear() = synchronized {
    clearRequest = false
  }

  def hasToClear = synchronized {
    clearRequest
  }
}

class DemoListener extends Listener {
  val cache = collection.mutable.Map[Long, Map[Int, Map[String, Double]]]()

  def messagesToListen = Array(classOf[CpuFormulaMessage], classOf[DiskFormulaMessage])

  init()

  def init() {
    SwingUtilities.invokeLater(new Runnable {
      def run() {
        Chart.run()
      }
    })
  }

  def acquire = {
    case cpuFormulaMessage: CpuFormulaMessage => process(cpuFormulaMessage.tick.timestamp, cpuFormulaMessage.tick.subscription.process.pid, "cpu", cpuFormulaMessage.energy.power)
    case diskFormulaMessage: DiskFormulaMessage => process(diskFormulaMessage.tick.timestamp, diskFormulaMessage.tick.subscription.process.pid, "disk", diskFormulaMessage.energy.power)
  }

  def process(timestamp: Long, pid: Int, device: String, power: Double) {
    if (DemoListener.hasToClear) {
      clear()
      DemoListener.unClear()
    }
    add(timestamp, pid, device, power)
    flush(timestamp)
  }

  def add(timestamp: Long, pid: Int, device: String, power: Double) {
    val cachedPid =
      if (DemoListener.isNamed(pid)) {
        pid
      } else {
        -1
      }
    val processes = cache.getOrElse(timestamp, Map[Int, Map[String, Double]]())
    val devices = processes.getOrElse(cachedPid, Map[String, Double]())
    val powers = devices.getOrElse(device, 0: Double)

    cache += timestamp -> (processes + (cachedPid -> (devices + (device -> (powers + power)))))
  }

  def flush(limit: Long) {
    cache.filter(entry => (entry._1 < limit)).foreach(value => {
      display(value._1)
      remove(value._1)
    })
  }

  def display(timestamp: Long) {
    if (DemoListener.hasToJustTotal) {
      cache(timestamp).foreach(process => {
        Chart.add(Map(
          DemoListener.pidNames(process._1) + " total" -> process._2.foldLeft(0: Double) { (acc, device) => acc + device._2 }), timestamp)
      })
    } else {
      cache(timestamp).foreach(process => {
        Chart.add(Map(
          DemoListener.pidNames(process._1) + " cpu" -> process._2.getOrElse("cpu", 0: Double),
          DemoListener.pidNames(process._1) + " disk" -> process._2.getOrElse("disk", 0: Double)), timestamp)
      })
    }
  }

  def remove(timestamp: Long) {
    cache -= timestamp
  }

  def clear() {
    cache.clear()
  }

}