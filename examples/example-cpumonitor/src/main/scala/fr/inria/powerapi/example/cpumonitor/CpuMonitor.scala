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
package fr.inria.powerapi.example.cpumonitor

import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor

/**
 * CPU monitoring example that deals with different use cases.
 *
 * @see Processes
 *
 * @author abourdon
 */
object CpuMonitor {
  private def beforeStart() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))
  }

  private def beforeEnd() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

  def main(args: Array[String]) {
    beforeStart()
    Processes.intensive
    beforeEnd()
  }
}
