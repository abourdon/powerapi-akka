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
package fr.inria.powerapi.sensor.disk.atop
import java.io.IOException
import fr.inria.powerapi.sensor.disk.api.DiskSensorMessage
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Clock
import fr.inria.powerapi.core.TickIt
import fr.inria.powerapi.core.UnTickIt
import fr.inria.powerapi.core.TickSubscription
import scalax.io.Resource
import java.io.FileInputStream
import java.net.URL
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import scala.util.Properties
import org.junit.Test
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.actor.Props
import akka.util.duration._

trait ConfigurationMock extends Configuration {
  lazy val basedir = new URL("file", Properties.propOrEmpty("basedir"), "")
  override lazy val processStatPath = new URL(basedir, "/src/test/resources/proc/%?/stat").toString
}

class DiskSensorMock extends DiskSensor with ConfigurationMock

class DiskSensorReceiver extends Listener {
  val receivedData = collection.mutable.Map[String, (Long, Long)]()

  def messagesToListen = Array(classOf[DiskSensorMessage])

  def acquire = {
    case diskSensorMessage: DiskSensorMessage => process(diskSensorMessage)
  }

  def process(diskSensorMessage: DiskSensorMessage) {
    receivedData ++= diskSensorMessage.rw
  }
}

class DiskSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Test
  def testReadAndWrite() {
    implicit val system = ActorSystem("DiskSensorSuite")
    val diskSensor = TestActorRef[DiskSensorMock].underlyingActor

    diskSensor.readAndWrite(Process(123)) should equal((1, 3))
  }

  @Test
  def testTick() {
    implicit val system = ActorSystem("DiskSensorSuite")
    val diskSensor = system.actorOf(Props[DiskSensorMock])
    val diskSensorReceiver = TestActorRef[DiskSensorReceiver]
    val clock = system.actorOf(Props[Clock])
    system.eventStream.subscribe(diskSensor, classOf[Tick])
    system.eventStream.subscribe(diskSensorReceiver, classOf[DiskSensorMessage])

    clock ! TickIt(TickSubscription(Process(123), 10 seconds))
    Thread.sleep(1000)
    clock ! UnTickIt(TickSubscription(Process(123), 10 seconds))

    diskSensorReceiver.underlyingActor.receivedData should have size 1
    diskSensorReceiver.underlyingActor.receivedData("n/a") should equal((1.0, 3.0))
  }
}