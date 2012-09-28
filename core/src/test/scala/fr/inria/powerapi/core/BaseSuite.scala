/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.core

import akka.actor.{Props, ActorSystem}
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import org.junit.Test
import org.scalatest.junit.{ShouldMatchersForJUnit, JUnitSuite}


case class FooMessage() extends Message

case class BarMessage() extends Message

class SimpleActor extends Component {
  def messagesToListen = Array(classOf[FooMessage], classOf[BarMessage])

  def acquire = {
    case str: String => sender ! str
  }
}

class BaseSuite extends JUnitSuite with ShouldMatchersForJUnit {
  val system = ActorSystem("base-suite")
  val simpleActor = system.actorOf(Props[SimpleActor])
  implicit val timeout = Timeout(5 seconds)

  @Test
  def testMessagesToListen() {
    val request = simpleActor ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 2
    messages(0) should be(classOf[FooMessage])
    messages(1) should be(classOf[BarMessage])
  }

  @Test
  def testListen() {
    val request = simpleActor ? "hello"
    val answer = Await.result(request, timeout.duration).asInstanceOf[String]

    answer should equal("hello")
  }
}