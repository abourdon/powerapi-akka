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
package powerapi.sensor.cpusensor
import powerapi.core.Tick
import powerapi.core.Message
import powerapi.core.Sensor

/** Messages definition */
case class TimeInStates(times: Map[Int, Long]) extends Message {
  def -(that: TimeInStates) =
    TimeInStates((for ((frequency, time) <- times) yield (frequency, time - that.times.getOrElse(frequency, 0: Long))).toMap)
}
case class GlobalElapsedTime(time: Int) extends Message
case class ProcessElapsedTime(time: Int) extends Message
case class CpuSensorValues(
  timeInStates: TimeInStates,
  globalElapsedTime: GlobalElapsedTime,
  processElapsedTime: ProcessElapsedTime,
  tick: Tick) extends Message

trait CpuSensor extends Sensor {
  def messagesToListen = Array(classOf[Tick])

  def process(tick: Tick)

  def listen = {
    case tick: Tick => process(tick)
  }
}