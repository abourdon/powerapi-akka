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
package fr.inria.powerapi.formula.mem.api

import fr.inria.powerapi.core.Formula
import fr.inria.powerapi.sensor.mem.api.MemSensorMessage
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.FormulaMessage

/**
 * Memory formula's message definition
 *
 * @author abourdon
 */
case class MemFormulaMessage(energy: Energy, tick: Tick, device: String = "memory") extends FormulaMessage

/**
 * Base trait for each Memory formula implementation module.
 *
 * @author abourdon
 */
trait MemFormula extends Formula {

  def messagesToListen = Array(classOf[MemSensorMessage])

  def process(memSensorMessage: MemSensorMessage)

  def acquire = {
    case memSensorMessage: MemSensorMessage => process(memSensorMessage)
  }

}