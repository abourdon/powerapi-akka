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
package fr.inria.powerapi.example.monitor.windows

import fr.inria.powerapi.library.PowerAPI

object Initializer {
  def beforeStart() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.sigar.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.max.CpuFormula],
      classOf[fr.inria.powerapi.sensor.mem.sigar.MemSensor],
      classOf[fr.inria.powerapi.formula.mem.single.MemFormula]
    ).foreach(PowerAPI.startEnergyModule(_))
  }

  def beforeEnd() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.sigar.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.max.CpuFormula],
      classOf[fr.inria.powerapi.sensor.mem.sigar.MemSensor],
      classOf[fr.inria.powerapi.formula.mem.single.MemFormula]
    ).foreach(PowerAPI.stopEnergyModule(_))
  }
}

/**
 * Monitoring example that deals with different use cases.
 *
 * @see Processes
 *
 * @author abourdon
 */
object Monitor {

  def main(args: Array[String]) {
    Initializer.beforeStart()
    Processes.current
    Initializer.beforeEnd()
  }

}