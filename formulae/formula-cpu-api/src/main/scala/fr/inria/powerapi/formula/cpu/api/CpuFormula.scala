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
package fr.inria.powerapi.formula.cpu.api

import fr.inria.powerapi.core.{Energy, Formula, Tick, Message}
import fr.inria.powerapi.sensor.cpu.api.CpuSensorValues

/**
 * CPU formula messages definition.
 *
 * @author abourdon
 */
case class CpuFormulaValues(energy: Energy, tick: Tick) extends Message

/**
 * Base trait for CPU sensor modules.
 *
 * Each of these have to listen to the CpuSensorValues message and implements the associated process method.
 *
 * @author abourdon
 */
trait CpuFormula extends Formula {
  def messagesToListen = Array(classOf[CpuSensorValues])

  def process(cpuSensorValues: CpuSensorValues)

  def process = {
    case cpuSensorValues: CpuSensorValues => process(cpuSensorValues)
  }
}