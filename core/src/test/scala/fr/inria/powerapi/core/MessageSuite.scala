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

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

class MessageSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testLeafMessage {
    val system = ActorSystem("system")

    case class LeafMessage(tick: Tick) extends SensorMessage
    val actor = system.actorOf(Props(new Actor() {
      def receive = {
        case leaf: LeafMessage => sender ! true
        case _ => sender ! false
      }
    }))

    implicit val timeout = Timeout(5.seconds)
    val result = Await.result(actor ? LeafMessage(null), timeout.duration).asInstanceOf[Boolean]

    result should equal(true)
  }

  @Test
  def testNodeMessage {
    val system = ActorSystem("system")

    case class LeafMessage(energy: Energy, tick: Tick, device: String) extends FormulaMessage
    val actor = system.actorOf(Props(new Actor() {
      def receive = {
        case node: FormulaMessage => sender ! true
        case _ => sender ! false
      }
    }))

    implicit val timeout = Timeout(5.seconds)
    val result = Await.result(actor ? LeafMessage(null, null, null), timeout.duration).asInstanceOf[Boolean]

    result should equal(true)
  }

}