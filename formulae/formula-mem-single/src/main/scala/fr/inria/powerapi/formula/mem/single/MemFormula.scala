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
package fr.inria.powerapi.formula.mem.single

import fr.inria.powerapi.sensor.mem.api.MemSensorMessage
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.formula.mem.api.MemFormulaMessage

/**
 * MemFormula specific configuration part.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {

  /**
   * Consumed power by the RAM when reading data.
   */
  lazy val readPower = load { _.getDouble("powerapi.mem.read-power") }(0)

  /**
   * Consumed power by the RAM when writing data.
   */
  lazy val writePower = load { _.getDouble("powerapi.mem.write-power") }(0)

}

/**
 * Computes the energy consumed by the memory based on a simple computation: RAM sticks are seen as a single one.
 *
 * From the power point of view, this "single" RAM power is computing in averaging the reading and writing power.
 * So, be careful to give the whole power consumption of this "single" RAM which is simply the addition of the power consumption of each RAM stick contained into this single one.
 *
 * @author abourdon
 */
class MemFormula extends fr.inria.powerapi.formula.mem.api.MemFormula with Configuration {

  lazy val power = (readPower + writePower).doubleValue / 2

  def compute(memSensorMessage: MemSensorMessage) = power * memSensorMessage.residentPerc

  def process(memSensorMessage: MemSensorMessage) {
    publish(MemFormulaMessage(Energy.fromPower(compute(memSensorMessage)), memSensorMessage.tick))
  }

}
