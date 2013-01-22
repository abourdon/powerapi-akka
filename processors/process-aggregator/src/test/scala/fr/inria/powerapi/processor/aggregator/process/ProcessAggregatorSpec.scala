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
package fr.inria.powerapi.processor.aggregator.process

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import org.junit.runner.RunWith
import org.scalatest.junit.ShouldMatchersForJUnit
import fr.inria.powerapi.processor.aggregator.timestamp.AggregatedMessage
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import fr.inria.powerapi.core.FormulaMessage
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.core.Process

case class FormulaMessageMock(energy: Energy, tick: Tick, device: String = "mock") extends FormulaMessage

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
    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), 1), device = "cpu"))
    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(2), Tick(TickSubscription(Process(123), 1 second), 1), device = "mem"))
    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(3), Tick(TickSubscription(Process(345), 1 second), 1), device = "cpu"))

    processAggregator.underlyingActor.sent should be('empty)

    processAggregator.underlyingActor.process(FormulaMessageMock(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), 2), device = "cpu"))

    processAggregator.underlyingActor.sent should have size 2
    processAggregator.underlyingActor.sent should (contain key (Process(123)) and contain key (Process(345)))
    processAggregator.underlyingActor.sent(Process(123)).energy should equal(Energy.fromPower(1 + 2))
    processAggregator.underlyingActor.sent(Process(345)).energy should equal(Energy.fromPower(3))
  }
}