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
package fr.inria.powerapi.processor.aggregator.device

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import org.junit.runner.RunWith
import org.scalatest.junit.ShouldMatchersForJUnit
import fr.inria.powerapi.processor.aggregator.timestamp.AggregatedMessage
import org.scalatest.FlatSpec
import fr.inria.powerapi.core.FormulaMessage
import org.scalatest.junit.JUnitRunner
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.core.Process

case class FormulaMessageMock(energy: Energy, tick: Tick, device: String = "mock") extends FormulaMessage

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
    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), 1), device = "cpu"))
    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(2), Tick(TickSubscription(Process(345), 1 second), 1), device = "cpu"))
    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(3), Tick(TickSubscription(Process(123), 1 second), 1), device = "mem"))

    deviceAggregator.underlyingActor.sent should be('empty)

    deviceAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), 2), device = "cpu"))

    deviceAggregator.underlyingActor.sent should have size 2
    deviceAggregator.underlyingActor.sent should (contain key ("cpu") and contain key ("mem"))
    deviceAggregator.underlyingActor.sent("cpu").energy should equal(Energy.fromPower(1 + 2))
    deviceAggregator.underlyingActor.sent("mem").energy should equal(Energy.fromPower(3))
  }
}