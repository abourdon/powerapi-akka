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
package fr.inria.powerapi.sensor.cpu.proc

import java.net.URL
import scala.util.Properties
import org.junit.Test
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Clock
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickIt
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.core.UnTickIt
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.GlobalElapsedTime
import fr.inria.powerapi.sensor.cpu.api.ProcessElapsedTime
import fr.inria.powerapi.sensor.cpu.api.TimeInStates
import org.scalatest.junit.JUnitSuite

class CpuSensorReceiver extends Actor {
  var receivedData: Option[CpuSensorMessage] = None

  def receive = {
    case cpuSensorMessage: CpuSensorMessage => receivedData = Some(cpuSensorMessage)
  }
}

class CpuSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {

  trait ConfigurationMock extends Configuration {
    override lazy val cores = 4

    lazy val basedir = new URL("file", Properties.propOrEmpty("basedir"), "")
    override lazy val globalStatPath = new URL(basedir, "/src/test/resources/proc/stat").toString
    override lazy val processStatPath = new URL(basedir, "/src/test/resources/proc/%?/stat").toString
    override lazy val timeInStatePath = new URL(basedir, "/src/test/resources/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state").toString
  }

  implicit val system = ActorSystem("cpusensorsuite")
  implicit val tick = Tick(TickSubscription(Process(123), 1 second))
  val cpuSensor = TestActorRef(new CpuSensor with ConfigurationMock)

  @Test
  def testTimeInStates() {
    testTimeInStates(TimeInStates(cpuSensor.underlyingActor.timeInStates))
  }

  private def testTimeInStates(timeInStates: TimeInStates) {
    timeInStates.times should have size 4
    timeInStates.times(4000000) should equal(16)
    timeInStates.times(3000000) should equal(12)
    timeInStates.times(2000000) should equal(8)
    timeInStates.times(1000000) should equal(4)
  }

  @Test
  def testTimeInStatesWhenNotEnabled() {
    val cpuSensor = TestActorRef(new CpuSensor with ConfigurationMock {
      override lazy val timeInStateEnabled = false
    })
    testTimeInStatesWhenNotEnabled(TimeInStates(cpuSensor.underlyingActor.timeInStates))
  }

  private def testTimeInStatesWhenNotEnabled(timeInStates: TimeInStates) {
    timeInStates.times should have size 0

  }

  @Test
  def testGlobalElapsedTime() {
    testGlobalElapsedTime(GlobalElapsedTime(cpuSensor.underlyingActor.elapsedTime))
  }

  private def testGlobalElapsedTime(globalElapsedTime: GlobalElapsedTime) {
    globalElapsedTime.time should equal(441650 + 65 + 67586 + 3473742 + 31597 + 0 + 7703 + 0 + 0 + 0)
  }

  @Test
  def testProcessElapsedTime() {
    testProcessElapsedTime(ProcessElapsedTime(cpuSensor.underlyingActor.elapsedTime(Process(123))))
  }

  private def testProcessElapsedTime(processElapsedTime: ProcessElapsedTime) {
    processElapsedTime.time should equal(2 + 2)
  }

  @Test
  def testTick() {
    val cpuSensorReceiver = TestActorRef[CpuSensorReceiver]
    val clock = system.actorOf(Props[Clock])
    system.eventStream.subscribe(cpuSensor, classOf[Tick])
    system.eventStream.subscribe(cpuSensorReceiver, classOf[CpuSensorMessage])

    clock ! TickIt(TickSubscription(Process(123), 10 seconds))
    Thread.sleep(1000)
    clock ! UnTickIt(TickSubscription(Process(123), 10 seconds))

    cpuSensorReceiver.underlyingActor.receivedData match {
      case None => fail()
      case Some(cpuSensorMessage) => {
        testTimeInStates(cpuSensorMessage.timeInStates)
        testGlobalElapsedTime(cpuSensorMessage.globalElapsedTime)
        testProcessElapsedTime(cpuSensorMessage.processElapsedTime)
      }
    }
  }
}