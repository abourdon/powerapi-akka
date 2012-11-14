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
package fr.inria.powerapi.formula.cpu.dvfs

import scala.collection.JavaConversions
import scala.collection.mutable

import com.typesafe.config.Config

import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage

/**
 * CPU formula configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * CPU Thermal Dissipation Power value.
   *
   * @see http://en.wikipedia.org/wiki/CPU_power_dissipation
   */
  lazy val tdp = load { _.getDouble("powerapi.cpu.tdp") }(0)

  /**
   * CPU cores number.
   */
  lazy val cores = load { _.getDouble("powerapi.cpu.cores") }(1)

  /**
   * Map of frequencies and their associated voltages.
   */
  lazy val frequencies = load {
    conf =>
      (for (item <- JavaConversions.asScalaBuffer(conf.getConfigList("powerapi.cpu.frequencies")))
        yield (item.asInstanceOf[Config].getInt("value"), item.asInstanceOf[Config].getDouble("voltage"))).toMap
  }(Map[Int, Double]())
}

/**
 * CPU formula component giving CPU energy of a given process in computing the ratio between
 * global CPU energy and process CPU usage during a given period.
 *
 * Global CPU energy is given thanks to the well-known global formula: P = c * f * V² [1].
 * This formula operates for an unique frequency/variable but many frequencies can be used by CPU during a time period (e.g using DVFS [2]).
 * Thus, this implementation weights each frequency by the time spent by CPU in working under it.
 *
 * Process CPU usage is computed in making the ratio between global and process CPU time usage.
 * Thus processUsage = processTimeUsage / globalTimeUsage.
 *
 * @see [1] "Frequency–Voltage Cooperative CPU Power Control: A Design Rule and Its Application by Feedback Prediction" by Toyama & al.
 * @see [2] http://en.wikipedia.org/wiki/Voltage_and_frequency_scaling.
 *
 * @author abourdon
 */
class CpuFormula extends fr.inria.powerapi.formula.cpu.api.CpuFormula with Configuration {

  import collection.mutable

  lazy val constant = (0.7 * tdp) / (frequencies.max._1 * math.pow(frequencies.max._2, 2))
  lazy val powers = frequencies.map(frequency => (frequency._1, (constant * frequency._1 * math.pow(frequency._2, 2))))

  lazy val cache = mutable.HashMap[TickSubscription, CpuSensorMessage]()

  def process(cpuSensorMessage: CpuSensorMessage) {
    publish(compute(cpuSensorMessage))
    refreshCache(cpuSensorMessage)
  }

  def usage(old: CpuSensorMessage, now: CpuSensorMessage) = {
    val processUsage = (now.processElapsedTime.time - old.processElapsedTime.time).toDouble
    val globalUsage = (now.globalElapsedTime.time - old.globalElapsedTime.time).toDouble
    if (globalUsage == 0) {
      0.0
    } else {
      math.max(0.0, processUsage / globalUsage)
    }
  }

  def power(old: CpuSensorMessage, now: CpuSensorMessage) = {
    val timeInStates = now.timeInStates - old.timeInStates
    val totalPower = powers.foldLeft(0: Double) {
      (acc, power) => acc + (power._2 * timeInStates.times.getOrElse(power._1, 0: Long))
    }
    val time = timeInStates.times.foldLeft(0: Long) {
      (acc, time) => acc + time._2
    }
    if (time == 0) {
      0.0
    } else {
      totalPower / time
    }

  }

  def compute(now: CpuSensorMessage): CpuFormulaMessage = {
    val old = cache getOrElse (now.tick.subscription, now)
    CpuFormulaMessage(Energy.fromPower(power(old, now) * usage(old, now)), now.tick)
  }

  def refreshCache(now: CpuSensorMessage) {
    cache += (now.tick.subscription -> now)
  }
}