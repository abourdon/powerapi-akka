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

import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

import org.junit.Ignore
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.dispatch.Await
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import akka.util.duration.intToDurationInt

case object Result

class ByProcessTickReceiver extends akka.actor.Actor with ActorLogging {
  val receivedTicks = new HashMap[TickSubscription, Int] with SynchronizedMap[TickSubscription, Int]

  private def increment(tickSubscription: TickSubscription) {
    val currentTick = receivedTicks getOrElse(tickSubscription, 0)
    receivedTicks += (tickSubscription -> (currentTick + 1))
  }

  def receive = {
    case tick: Tick => increment(tick.subscription)
    case Result => sender ! receivedTicks
    case unknown => throw new UnsupportedOperationException("unable to process message " + unknown)
  }
}

class SimpleTickReceiver extends akka.actor.Actor with ActorLogging {
  var receivedTicks = 0

  def receive = {
    case tick: Tick => receivedTicks += 1
  }
}

class ClockSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("ClockTest")
  val clock = TestActorRef[Clock]

  @Test
  def testMessagesToListen() {
    implicit val timeout = Timeout(5 seconds)
    val request = clock ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 2
    messages(0) should be(classOf[TickIt])
    messages(1) should be(classOf[UnTickIt])
  }

  @Test
  def testReceivedSimpleTicks() {
    val tickReceiver = TestActorRef[ByProcessTickReceiver]
    system.eventStream.subscribe(tickReceiver, classOf[Tick])

    clock ! TickIt(TickSubscription(Process(123), 500 milliseconds))
    clock ! TickIt(TickSubscription(Process(124), 1000 milliseconds))
    clock ! TickIt(TickSubscription(Process(125), 1500 milliseconds))
    Thread.sleep(3200)

    clock ! UnTickIt(TickSubscription(Process(123), 500 milliseconds))
    Thread.sleep(2200)

    clock ! UnTickIt(TickSubscription(Process(124), 500 milliseconds))
    clock ! UnTickIt(TickSubscription(Process(125), 1500 milliseconds))

    val receivedTicks = tickReceiver.underlyingActor.receivedTicks
    receivedTicks getOrElse(TickSubscription(Process(123), 500 milliseconds), 0) should {
      equal(7) or equal(7 + 1)
    }
    receivedTicks getOrElse(TickSubscription(Process(124), 1000 milliseconds), 0) should {
      equal(5) or equal(5 + 1)
    }
    receivedTicks getOrElse(TickSubscription(Process(125), 1500 milliseconds), 0) should {
      equal(4) or equal(4 + 1)
    }
  }

  @Ignore
  @Test
  def testReceivedIntensiveTicks() {
    val tickReceiver = TestActorRef[SimpleTickReceiver]
    val duration = 100 milliseconds
    val sleep = 10 seconds
    val pids = (0 to 500)
    system.eventStream.subscribe(tickReceiver, classOf[Tick])

    pids.foreach(pid => clock ! TickIt(TickSubscription(Process(pid), duration)))
    Thread.sleep(sleep.toMillis)
    pids.foreach(pid => clock ! UnTickIt(TickSubscription(Process(pid), duration)))
    Thread.sleep(sleep.toMillis / 2)

    val averageReceivedTicks = tickReceiver.underlyingActor.receivedTicks.toDouble / pids.size
    println(averageReceivedTicks)
  }
}