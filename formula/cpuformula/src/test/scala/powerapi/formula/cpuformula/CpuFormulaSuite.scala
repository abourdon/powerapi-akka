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
package powerapi.formula.cpuformula

import akka.actor.ActorSystem
import akka.actor.Props
import akka.dispatch.Await
import akka.util.Timeout
import akka.util.duration._
import akka.pattern.ask

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

import powerapi.core.Message
import powerapi.core.MessagesToListen
import powerapi.sensor.cpusensor.CpuSensorValues

class CpuFormulaMock extends CpuFormula {
  def process(cpuSensorValues: CpuSensorValues) {}
}

class CpuFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {
  lazy val system = ActorSystem("CpuFormulaSuite")
  implicit lazy val timeout = Timeout(5 seconds)

  @Test
  def testMessagesToListen {
    val cpuFormula = system.actorOf(Props[CpuFormulaMock], name = "cpuFormulaMock")
    val request = cpuFormula ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 1
    messages(0) should be(classOf[CpuSensorValues])
  }

}