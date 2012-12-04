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
package fr.inria.powerapi.sensor.cpu.api

import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Sensor
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.SensorMessage

/**
 * CPU sensor's messages definition.
 *
 * @author abourdon
 */
case class TimeInStates(times: Map[Int, Long]) {
  def -(that: TimeInStates) =
    TimeInStates((for ((frequency, time) <- times) yield (frequency, time - that.times.getOrElse(frequency, 0: Long))).toMap)
}
case class GlobalElapsedTime(time: Long)
case class ProcessElapsedTime(time: Long)
case class ProcessPercent(percent: Double)

case class CpuSensorMessage(
  timeInStates: TimeInStates = TimeInStates(Map[Int, Long]()),
  globalElapsedTime: GlobalElapsedTime = GlobalElapsedTime(-1),
  processElapsedTime: ProcessElapsedTime = ProcessElapsedTime(-1),
  processPercent: ProcessPercent = ProcessPercent(-1),
  tick: Tick) extends SensorMessage

/**
 * Base trait for CPU sensor modules.
 *
 * Each of these has to listen to the Tick message and implements the associated process method.
 *
 * @author abourdon
 */
trait CpuSensor extends Sensor {
  def messagesToListen = Array(classOf[Tick])

  def process(tick: Tick)

  def acquire = {
    case tick: Tick => process(tick)
  }
}