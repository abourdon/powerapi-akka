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
package fr.inria.powerapi.listener.aggregator

import scala.concurrent.duration.DurationInt

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.FormulaMessage
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription

case class FormulaMessageMock(energy: Energy, tick: Tick, device: String = "mock") extends FormulaMessage

class TimestampAggregatorMock extends TimestampAggregator {
  val sent = collection.mutable.Map[Long, AggregatedMessage]()

  override def send(implicit timestamp: Long) {
    sent += timestamp -> cache(timestamp)
  }
}

@RunWith(classOf[JUnitRunner])
class TimestampAggregatorSpec extends FlatSpec with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("timestamp-aggregator-spec")
  val timestampAggregator = TestActorRef[TimestampAggregatorMock]

  "A TimestampAggregator" should "listen to FormulaMessage" in {
    timestampAggregator.underlyingActor.messagesToListen should equal(Array(classOf[FormulaMessage]))
  }

  "A TimestampAggregator" should "process a FormulaMessage" in {
    timestampAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 1)))
    timestampAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(2), Tick(TickSubscription(Process(123), 1.second), 1)))
    timestampAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(3), Tick(TickSubscription(Process(123), 1.second), 1)))

    timestampAggregator.underlyingActor.sent should be('empty)

    timestampAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 2)))

    timestampAggregator.underlyingActor.sent should have size 1
    timestampAggregator.underlyingActor.sent should contain key 1
    timestampAggregator.underlyingActor.sent(1).energy should equal(Energy.fromPower(1 + 2 + 3))

    timestampAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 3)))

    timestampAggregator.underlyingActor.sent should have size 2
    timestampAggregator.underlyingActor.sent should contain key 2
    timestampAggregator.underlyingActor.sent(2).energy should equal(Energy.fromPower(1))
  }
}

class DeviceAggregatorMock extends DeviceAggregator {
  val sent = collection.mutable.Map[String, AggregatedMessage]()

  override def send(implicit timestamp: Long) {
    byDevices foreach (msg => sent += msg.device -> msg)
  }
}

@RunWith(classOf[JUnitRunner])
class DeviceAggregatorSpec extends FlatSpec with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("device-aggregator-spec")
  val deviceAggregator = TestActorRef[DeviceAggregatorMock]

  "A DeviceAggregator" should "listen to FormulaMessage" in {
    deviceAggregator.underlyingActor.messagesToListen should equal(Array(classOf[FormulaMessage]))
  }

  "A DeviceAggregator" should "process a FormulaMessage" in {
    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 1), device = "cpu"))
    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(2), Tick(TickSubscription(Process(345), 1.second), 1), device = "cpu"))
    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(3), Tick(TickSubscription(Process(123), 1.second), 1), device = "mem"))

    deviceAggregator.underlyingActor.sent should be('empty)

    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 2), device = "cpu"))

    deviceAggregator.underlyingActor.sent should have size 2
    deviceAggregator.underlyingActor.sent should (contain key ("cpu") and contain key ("mem"))
    deviceAggregator.underlyingActor.sent("cpu").energy should equal(Energy.fromPower(1 + 2))
    deviceAggregator.underlyingActor.sent("mem").energy should equal(Energy.fromPower(3))
  }
}

class ProcessAggregatorMock extends ProcessAggregator {
  val sent = collection.mutable.Map[Process, AggregatedMessage]()

  override def send(implicit timestamp: Long) {
    byProcesses foreach (msg => sent += msg.tick.subscription.process -> msg)
  }
}

@RunWith(classOf[JUnitRunner])
class ProcessAggregatorSpec extends FlatSpec with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("process-aggregator-spec")
  val processAggregator = TestActorRef[ProcessAggregatorMock]

  "A ProcessAggregator" should "listen to FormulaMessage" in {
    processAggregator.underlyingActor.messagesToListen should equal(Array(classOf[FormulaMessage]))
  }

  "A ProcessAggregator" should "process a FormulaMessage" in {
    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 1), device = "cpu"))
    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(2), Tick(TickSubscription(Process(123), 1.second), 1), device = "mem"))
    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(3), Tick(TickSubscription(Process(345), 1.second), 1), device = "cpu"))

    processAggregator.underlyingActor.sent should be('empty)

    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1.second), 2), device = "cpu"))

    processAggregator.underlyingActor.sent should have size 2
    processAggregator.underlyingActor.sent should (contain key (Process(123)) and contain key (Process(345)))
    processAggregator.underlyingActor.sent(Process(123)).energy should equal(Energy.fromPower(1 + 2))
    processAggregator.underlyingActor.sent(Process(345)).energy should equal(Energy.fromPower(3))
  }
}