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
package fr.inria.powerapi.formula.disk.api
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Formula
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.sensor.disk.api.DiskSensorMessage
import fr.inria.powerapi.core.FormulaMessage
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Tick

/**
 * Disk formula's message definition.
 *
 * @author abourdon
 */
case class DiskFormulaMessage(energy: Energy, tick: Tick, device: String = "disk") extends FormulaMessage

/**
 * Base trait for disk formula modules.
 *
 * Each of these has to listen to the DiskSensorMessage message and implements the associated process method.
 *
 * @author abourdon
 */
trait DiskFormula extends Formula {
  def messagesToListen = Array(classOf[DiskSensorMessage])

  def process(diskSensorMessage: DiskSensorMessage)

  def acquire = {
    case diskSensorMessage: DiskSensorMessage => process(diskSensorMessage)
  }
}