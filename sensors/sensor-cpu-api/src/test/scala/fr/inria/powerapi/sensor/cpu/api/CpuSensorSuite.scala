/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.sensor.cpu.api

import akka.actor.{Props, ActorSystem}
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import fr.inria.powerapi.core.{Tick, Message, MessagesToListen}
import org.junit.Test
import org.scalatest.junit.{ShouldMatchersForJUnit, JUnitSuite}

class CpuSensorMock extends CpuSensor {
  def process(tick: Tick) {}
}

class CpuSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  lazy val system = ActorSystem("CpuSensorSuite")
  implicit lazy val timeout = Timeout(5 seconds)

  @Test
  def testMessagesToListen() {
    val cpuSensor = system.actorOf(Props[CpuSensorMock], name = "cpuSensorMock")
    val request = cpuSensor ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 1
    messages(0) should be(classOf[Tick])
  }
}