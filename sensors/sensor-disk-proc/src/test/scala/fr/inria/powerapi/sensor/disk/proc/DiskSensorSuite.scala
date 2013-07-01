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
package fr.inria.powerapi.sensor.disk.proc
import java.net.URL

import scala.concurrent.duration.DurationInt
import scala.util.Properties

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestActorRef
import fr.inria.powerapi.core.Clock
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickIt
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.core.UnTickIt
import fr.inria.powerapi.sensor.disk.api.DiskSensorMessage

class DiskReceiverMock extends Actor {
  var receivedValues: Option[DiskSensorMessage] = None

  def receive = {
    case diskSensorMessage: DiskSensorMessage => receivedValues = Some(diskSensorMessage)
  }
}

class DiskSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  trait ConfigurationMock extends Configuration {
    lazy val basedir = new URL("file", Properties.propOrEmpty("basedir"), "")
    override lazy val iofile = new URL(basedir, "/src/test/resources/proc/%?/io").toString
  }

  implicit val system = ActorSystem("DiskSensorSuite")
  implicit val tick = Tick(TickSubscription(Process(123), 1.second))
  val diskSensor = TestActorRef(new DiskSensor with ConfigurationMock)

  @Test
  def testReadAndWrite() {
    testReadAndWrite(diskSensor.underlyingActor.readAndwrite(tick.subscription.process))
  }

  private def testReadAndWrite(readAndWrite: (Long, Long)) {
    readAndWrite should equal((3309568, 567 - 36))
  }

  @Test
  def testTick() {
    val diskReceiver = TestActorRef[DiskReceiverMock]
    val clock = system.actorOf(Props[Clock])
    system.eventStream.subscribe(diskSensor, classOf[Tick])
    system.eventStream.subscribe(diskReceiver, classOf[DiskSensorMessage])

    clock ! TickIt(TickSubscription(Process(123), 10.seconds))
    Thread.sleep(1000)
    clock ! UnTickIt(TickSubscription(Process(123), 10.seconds))

    diskReceiver.underlyingActor.receivedValues match {
      case None => fail()
      case Some(diskSensorMessage) => testReadAndWrite(diskSensorMessage.rw("n/a"))
    }
  }
}