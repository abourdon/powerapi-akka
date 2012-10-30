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
package fr.inria.powerapi.sensor.disk.api

import org.junit.Test
import org.scalatest.junit.{ JUnitSuite, ShouldMatchersForJUnit }

import akka.actor.{ ActorSystem, Props }
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.{ Message, MessagesToListen, Tick }

class DiskSensorMock extends DiskSensor {
  def process(tick: Tick) {
    // Nothing to do
  }
}

class DiskSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  lazy val system = ActorSystem("CpuSensorSuite")
  implicit lazy val timeout = Timeout(5 seconds)

  @Test
  def testMessagesToListen() {
    val diskSensor = system.actorOf(Props[DiskSensorMock], name = "diskSensorMock")
    val request = diskSensor ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 1
    messages(0) should be(classOf[Tick])
  }
}