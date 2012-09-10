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
package fr.inria.powerapi.formula.disk.api
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Formula
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.sensor.disk.api.DiskSensorValues

/**
 * Disk formula messages definition.
 *
 * @author abourdon
 */
case class DiskFormulaValues(energy: Energy, tick: Tick) extends Message

trait DiskFormula extends Formula {
  def messagesToListen = Array(classOf[DiskSensorValues])

  def process(diskSensorValues: DiskSensorValues)

  def acquire = {
    case diskSensorValues: DiskSensorValues => process(diskSensorValues)
  }
}