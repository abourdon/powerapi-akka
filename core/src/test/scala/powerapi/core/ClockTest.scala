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
package powerapi.core

import org.junit.Test
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.event.LoggingBus
import akka.util.duration.intToDurationInt
import akka.util.Timeout
import akka.dispatch.Await
import org.junit.Assert
import akka.util.Duration
import akka.util.duration._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import scala.collection.mutable.SynchronizedMap

case object Result

class ClockReceiver extends Actor with ActorLogging {
  val receivedTicks = new HashMap[TickSubscription, Int] with SynchronizedMap[TickSubscription, Int]

  private def incr(tickSubscription: TickSubscription) {
    val currentTick = receivedTicks getOrElse (tickSubscription, 0)
    receivedTicks += (tickSubscription -> (currentTick + 1))
  }

  def receive = {
    case tick: Tick => incr(tick.subscription)
    case Result => sender ! receivedTicks
    case unknown => throw new UnsupportedOperationException("unable to process message " + unknown)
  }
}

@Test
class ClockTest {
  val system = ActorSystem("ClockTest")
  val clock = system.actorOf(Props(new Clock), name = "clock")

  @Test
  def testReceivedTicks {
    val clockReceiver = system.actorOf(Props(new ClockReceiver), name = "clockReceiver")
    system.eventStream.subscribe(clockReceiver, classOf[Tick])

    clock ! Subscribe(TickSubscription(Process(123), 500 milliseconds))
    clock ! Subscribe(TickSubscription(Process(124), 1000 milliseconds))
    clock ! Subscribe(TickSubscription(Process(125), 1500 milliseconds))
    Thread.sleep(3200)

    clock ! Unsubscribe(TickSubscription(Process(123), 500 milliseconds))
    Thread.sleep(2200)

    clock ! Unsubscribe(TickSubscription(Process(124), 500 milliseconds))
    clock ! Unsubscribe(TickSubscription(Process(125), 1500 milliseconds))

    implicit val timeout = Timeout(5 seconds)
    val receivedTicks = (Await result ((clockReceiver ? Result), timeout.duration)).asInstanceOf[Map[TickSubscription, Int]]

    Assert.assertEquals(6, receivedTicks getOrElse (TickSubscription(Process(123), 500 milliseconds), 0))
    Assert.assertEquals(5, receivedTicks getOrElse (TickSubscription(Process(124), 1000 milliseconds), 0))
    Assert.assertEquals(4, receivedTicks getOrElse (TickSubscription(Process(125), 1500 milliseconds), 0))
  }
}