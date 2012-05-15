/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
package powerapi.formula.cpuformula

import powerapi.core.Formula
import powerapi.core.Energy
import powerapi.core.Tick
import powerapi.core.Message
import powerapi.sensor.cpusensor.CpuSensorValues

/** Messages definition */
case class CpuFormulaValues(energy: Energy, tick: Tick) extends Message

trait CpuFormula extends Formula {
  def messagesToListen = Array(classOf[CpuSensorValues])

  def process(cpuSensorValues: CpuSensorValues)

  def listen = {
    case cpuSensorValues: CpuSensorValues => process(cpuSensorValues)
  }
}