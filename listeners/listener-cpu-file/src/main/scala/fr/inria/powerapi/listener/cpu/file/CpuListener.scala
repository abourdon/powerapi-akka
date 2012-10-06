/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.listener.cpu.file

import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import scalax.file.Path
import scalax.io.Line
import scalax.io.Resource

/**
 * CpuListener's configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * The output file path.
   */
  lazy val filePath = load(_.getString("powerapi.listener.cpu-console.out-prefix") + System.nanoTime())(Path.createTempFile(prefix = "powerapi.listener-cpu-file", deleteOnExit = false).path)

  /**
   * If result has to be append to an existing file, or simply overrided.
   */
  lazy val append = load(_.getBoolean("powerapi.listener.cpu-console.append"))(true)

  /**
   * If this CPU listener has to simply write power, or the whole information contained into the CpuFormulaValues message.
   */
  lazy val justPower = load(_.getBoolean("powerapi.listener.cpu-console.just-power"))(false)
}

/**
 * CPU listener displaying received CpuFormulaValues into a file following properties
 * contained into a configuration file.
 *
 * @author abourdon
 */
class CpuListener extends Listener with Configuration {
  lazy val output = {
    log.info("using " + filePath + " as output file")
    Resource.fromFile(filePath)
  }

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

  def acquire = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
  }

  def messagesToListen = Array(classOf[CpuFormulaValues])
}