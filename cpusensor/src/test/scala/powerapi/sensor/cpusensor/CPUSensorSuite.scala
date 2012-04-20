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
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import powerapi.core.Process
import powerapi.core.Tick
import powerapi.core.TickSubscription
import akka.actor.Actor
import powerapi.core.TickSubscription
import akka.actor.Props
import powerapi.core.Clock
import powerapi.core.Subscribe
import powerapi.core.Unsubscribe

class CPUSensorReceiver extends Actor {
  var receivedData: Option[CPUSensorValues] = None

  def receive = {
    case cpuSensorValues: CPUSensorValues => receivedData = Some(cpuSensorValues)
  }
}

class CPUSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("cpusensorsuite")
  implicit val tick = Tick(TickSubscription(Process(123), 1 second))
  val cpuSensor = TestActorRef[CPUSensor]

  private def testTimeInStates(timeInStates: TimeInStates) {
    timeInStates.times.size should equal(4)
    timeInStates.times(4000000) should equal(16)
    timeInStates.times(3000000) should equal(12)
    timeInStates.times(2000000) should equal(8)
    timeInStates.times(1000000) should equal(4)
  }

  @Test
  def testTimeInStates {
    testTimeInStates(TimeInStates(cpuSensor.underlyingActor.timeInStates))
  }

  private def testGlobalElapsedTime(globalElapsedTime: GlobalElapsedTime) {
    globalElapsedTime.time should equal(441650 + 65 + 67586 + 3473742 + 31597 + 0 + 7703 + 0 + 0 + 0)
  }

  @Test
  def testGlobalElapsedTime {
    testGlobalElapsedTime(GlobalElapsedTime(cpuSensor.underlyingActor.elapsedTime))
  }

  private def testProcessElapsedTime(processElapsedTime: ProcessElapsedTime) {
    processElapsedTime.time should equal(2 + 2 + 34)
  }

  @Test
  def testProcessElapsedTime {
    testProcessElapsedTime(ProcessElapsedTime(cpuSensor.underlyingActor.elapsedTime(Process(123))))
  }

  @Test
  def testTick() {
    val cpuSensorReceiver = TestActorRef[CPUSensorReceiver]
    val clock = system.actorOf(Props[Clock])
    system.eventStream.subscribe(cpuSensor, classOf[Tick])
    system.eventStream.subscribe(cpuSensorReceiver, classOf[CPUSensorValues])

    clock ! Subscribe(TickSubscription(Process(123), 10 seconds))
    Thread.sleep(1000)
    clock ! Unsubscribe(TickSubscription(Process(123), 10 seconds))

    cpuSensorReceiver.underlyingActor.receivedData match {
      case None => fail
      case Some(cpuSensorValues) => {
        testTimeInStates(cpuSensorValues.timeInStates)
        testGlobalElapsedTime(cpuSensorValues.globalElapsedTime)
        testProcessElapsedTime(cpuSensorValues.processElapsedTime)
      }
    }

  }

}