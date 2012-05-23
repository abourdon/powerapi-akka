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
package fr.inria.powerapi.core

import akka.event.LoggingReceive

/**
 * Base trait for each PowerAPI message
 */
trait Message

/**
 * Request to have the array of Messages that an actor have to listen
 */
case object MessagesToListen extends Message

/**
 * Base trait for each PowerAPI module could be represented as an actor
 */
trait Component extends akka.actor.Actor with akka.actor.ActorLogging {
  def process: Receive

  def messagesToListen: Array[Class[_ <: Message]]

  private lazy val messages = messagesToListen
  private def listenToMessages: Receive = {
    case MessagesToListen => sender ! messages
  }

  def receive = LoggingReceive {
    listenToMessages orElse process
  }

  def publish(message: Message) {
    context.system.eventStream publish message
  }
}

/**
 * Base trait for each PowerAPI energy module
 */
trait EnergyModule extends Component

/**
 * Base trait for each PowerAPI sensor
 */
trait Sensor extends EnergyModule

/**
 * Base trait for each PowerAPI formula
 */
trait Formula extends EnergyModule

/**
 * Base trait for each PowerAPI listener
 */
trait Listener extends Component