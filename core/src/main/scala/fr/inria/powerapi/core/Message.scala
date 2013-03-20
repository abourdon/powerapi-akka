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
package fr.inria.powerapi.core
import akka.util.Duration

/**
 * Base types used to describe PowerAPI messages.
 *
 * PowerAPI is based on a modular and asynchronous architecture
 * where modules are centralized into a common event bus.
 * Each module communicates with others through immutable messages in using
 * the Akka library.
 *
 * @see http://akka.io
 *
 * @author abourdon
 */

/**
 * In addition to a specific Listener, each PowerAPI request is composed by:
 * - a process
 * - a time period or computation duration
 * Thus, a TickSubscription represents this PowerAPI request composition.
 */
case class TickSubscription(process: Process, duration: Duration)

/**
 * Each PowerAPI's request is created according to a specific time period.
 * Each time period is "ticked", as a clock "tick", according to a specific timestamp.
 * A Tick is a wrapper of this specific timestamp, according a given TickSubscription.
 */
case class Tick(subscription: TickSubscription, timestamp: Long = System.currentTimeMillis) extends Message with Ordering[Tick] {
  def compare(a: Tick, b: Tick) = a.timestamp compare b.timestamp
}

/**
 * Base trait for each PowerAPI message.
 */
trait Message

/**
 * Kind of message when it is related to a specific hardware device.
 * It is thus named "deviced" message.
 */
trait DevicedMessage extends Message {
  val device: String
}

/**
 * Kind of message when it is related to a specific Tick.
 */
trait TickedMessage extends Message {
  val tick: Tick
}

/**
 * Base trait for each Sensor message.
 */
trait SensorMessage extends TickedMessage

/**
 * Base trait for each Formula message.
 */
trait FormulaMessage extends TickedMessage with DevicedMessage {
  def energy: Energy
}

/**
 * Base trait for each ProcessedMessage
 */
trait ProcessedMessage extends TickedMessage with DevicedMessage {
  def energy: Energy
}