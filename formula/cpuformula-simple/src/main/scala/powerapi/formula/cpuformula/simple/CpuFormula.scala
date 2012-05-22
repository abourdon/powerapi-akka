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
package powerapi.formula.cpuformula.simple

import scala.collection.mutable.HashMap

import powerapi.core.{ TickSubscription, Energy }
import powerapi.formula.cpuformula.CpuFormulaValues
import powerapi.sensor.cpusensor.{ TimeInStates, ProcessElapsedTime, GlobalElapsedTime, CpuSensorValues }

class CpuFormula extends powerapi.formula.cpuformula.CpuFormula with Configuration {
  lazy val constant = (0.7 * tdp) / (frequencies.max._1 * math.pow(frequencies.max._2, 2))
  lazy val powers = frequencies.map(frequency => (frequency._1, (constant * frequency._1 * math.pow(frequency._2, 2))))

  lazy val cache = HashMap[TickSubscription, CpuSensorValues]()
  lazy val defaultSensorValue =
    CpuSensorValues(
      TimeInStates(frequencies.map(fv => (fv._1, 0: Long))),
      GlobalElapsedTime(0),
      ProcessElapsedTime(0),
      null)

  def process(cpuSensorValues: CpuSensorValues) {
    publish(compute(cpuSensorValues))
    refreshCache(cpuSensorValues)
  }

  def usage(old: CpuSensorValues, now: CpuSensorValues) = {
    val processUsage = (now.processElapsedTime.time - old.processElapsedTime.time).toDouble
    val globalUsage = (now.globalElapsedTime.time - old.globalElapsedTime.time).toDouble
    if (globalUsage == 0) {
      0.0
    } else {
      math.max(0.0, processUsage / globalUsage)
    }
  }

  def power(old: CpuSensorValues, now: CpuSensorValues) = {
    val timeInStates = now.timeInStates - old.timeInStates
    val totalPower = powers.foldLeft(0: Double) { (acc, power) => acc + (power._2 * timeInStates.times.getOrElse(power._1, 0: Long)) }
    val time = timeInStates.times.foldLeft(0: Long) { (acc, time) => acc + time._2 }
    if (time == 0) {
      0.0
    } else {
      totalPower / time
    }

  }

  def compute(now: CpuSensorValues): CpuFormulaValues = {
    val old = cache getOrElse (now.tick.subscription, defaultSensorValue)
    CpuFormulaValues(Energy.fromPower(power(old, now) * usage(old, now)), now.tick)
  }

  def refreshCache(now: CpuSensorValues) {
    cache += (now.tick.subscription -> now)
  }
}