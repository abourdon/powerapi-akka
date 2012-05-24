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
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.listener.cpu.file
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.core.Listener
import scalax.io.Resource
import scalax.io.Line

class CpuListener extends Listener with Configuration {
  lazy val output = Resource.fromFile(filePath)

  def process(cpuFormulaValues: CpuFormulaValues) {
    val toWrite =
      if (justPower) {
        cpuFormulaValues.energy.power.toString
      } else {
        cpuFormulaValues.toString
      }

    if (append) {
      output.append(toWrite + Line.Terminators.NewLine.sep)
    } else {
      output.truncate(0)
      output.write(toWrite)
    }
  }

  def process = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
  }

  def messagesToListen = Array(classOf[CpuFormulaValues])
}