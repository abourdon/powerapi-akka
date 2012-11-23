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
package fr.inria.powerapi.formula.cpu.max

import scala.collection.mutable

import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage

/**
 * CpuFormula configuration part.
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {

  /**
   * CPU Thermal Design Power (TDP) value.
   *
   * @see http://en.wikipedia.org/wiki/Thermal_design_power
   */
  lazy val tdp = load { _.getInt("powerapi.cpu.tdp") }(0)
}

/**
 * Implements a CpuFormula in making the ratio between maximum CPU power (obtained by multiplying
 * its Thermal Design Power (TDP) value by 0.7 [1]) and the process CPU usage obtained from
 * the received CpuSensorMessage.
 *
 * @see http://en.wikipedia.org/wiki/Thermal_design_power
 * @see [1], JouleSort: A Balanced Energy-Efﬁciency Benchmark, by Rivoire et al.
 */
class CpuFormula extends fr.inria.powerapi.formula.cpu.api.CpuFormula with Configuration {
  lazy val power = tdp * 0.7
  lazy val cache = mutable.HashMap[TickSubscription, CpuSensorMessage]()

  def usage(now: CpuSensorMessage, old: CpuSensorMessage) = {
    val processUsage = (now.processElapsedTime.time - old.processElapsedTime.time).toDouble
    val globalUsage = (now.globalElapsedTime.time - old.globalElapsedTime.time).toDouble
    if (globalUsage == 0) {
      0.0
    } else {
      math.max(0.0, processUsage / globalUsage)
    }
  }

  def compute(now: CpuSensorMessage) = {
    val old = cache.getOrElse(now.tick.subscription, now)
    Energy.fromPower(power * usage(now, old))
  }

  def refreshCache(now: CpuSensorMessage) {
    cache += (now.tick.subscription -> now)
  }

  def process(cpuSensorMessage: CpuSensorMessage) {
    publish(CpuFormulaMessage(compute(cpuSensorMessage), cpuSensorMessage.tick))
    refreshCache(cpuSensorMessage)
  }

}