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
package fr.inria.powerapi.listener.cpu.console

import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage

/**
 * CPU listener which simply display received CpuFormulaMessage to the console.
 *
 * @author abourdon
 */
class CpuListener extends Listener {
  def messagesToListen = Array(classOf[CpuFormulaMessage])

  def process(cpuFormulaMessage: CpuFormulaMessage) {
    println(cpuFormulaMessage)
  }

  def acquire = {
    case cpuFormulaMessage: CpuFormulaMessage => process(cpuFormulaMessage)
  }
}