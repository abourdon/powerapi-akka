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
import scala.collection.mutable.HashMap
import akka.actor.Actor
import akka.util.Duration
import powerapi.core.Configuration
import powerapi.core.Energy
import powerapi.core.Tick
import powerapi.sensor.cpusensor.CPUSensorValues
import powerapi.sensor.cpusensor.GlobalElapsedTime
import powerapi.sensor.cpusensor.ProcessElapsedTime
import powerapi.sensor.cpusensor.TimeInStates
import akka.actor.ActorLogging

/** Messages definition */
case class CPUFormulaValues(energy: Energy, tick: Tick)

class CPUFormula extends Actor with Configuration with ActorLogging {
  // Environment specific values (from the configuration file)
  lazy val tdp = fromConf("tdp") { node => (node \\ "@value").text.toDouble }(0)
  lazy val cores = fromConf("cores") { elt => (elt \\ "@value").text.toInt }(0)
  lazy val voltages = fromConf("frequency") { node => ((node \\ "@value").text.toInt, (node \\ "@voltage").text.toDouble) }.toMap
  lazy val constant = (0.7 * tdp) / (voltages.max._1 * math.pow(voltages.max._2, 2))
  lazy val powers = voltages.map(voltage => (voltage._1, (constant * voltage._1 * math.pow(voltage._2, 2))))

  lazy val cache = HashMap[Duration, CPUSensorValues]()
  lazy val defaultSensorValue =
    CPUSensorValues(
      TimeInStates(voltages.map(fv => (fv._1, 0))),
      GlobalElapsedTime(0),
      ProcessElapsedTime(0),
      null
    )

  def process(implicit cpuSensorValues: CPUSensorValues) {
    publish(compute)
    refreshCache
  }

  def usage(old: CPUSensorValues, now: CPUSensorValues): Double = {
    val processUsage = (now.processElapsedTime.time - old.processElapsedTime.time).toDouble
    val globalUsage = (now.globalElapsedTime.time - old.globalElapsedTime.time).toDouble / cores
    if (globalUsage == 0) {
      0.0
    } else {
      math.max(0.0, math.min(processUsage / globalUsage, 1.0))
    }
  }

  def power(old: CPUSensorValues, now: CPUSensorValues): Double = {
    val timeInStates = now.timeInStates - old.timeInStates
    val totalPower = powers.foldLeft(0: Double) { (acc, power) => acc + (power._2 * timeInStates.times.getOrElse(power._1, 0)) }
    val time = timeInStates.times.foldLeft(0) { (acc, time) => acc + time._2 }
    if (time == 0) {
      0.0
    } else {
      totalPower / time
    }

  }

  def compute(implicit now: CPUSensorValues): CPUFormulaValues = {
    val old = cache getOrElse (now.tick.subscription.duration, defaultSensorValue)
    val computed = power(old, now) * usage(old, now)
    CPUFormulaValues(Energy.fromPower(computed), now.tick)
  }

  def publish(implicit processed: CPUFormulaValues) {
    context.system.eventStream publish processed
  }

  def refreshCache(implicit now: CPUSensorValues) {
    cache += (now.tick.subscription.duration -> now)
  }

  def receive = {
    case cpuSensorValues: CPUSensorValues => process(cpuSensorValues)
  }
}