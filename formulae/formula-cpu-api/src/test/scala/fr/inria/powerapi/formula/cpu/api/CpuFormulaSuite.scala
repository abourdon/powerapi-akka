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
package fr.inria.powerapi.formula.cpu.api

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import akka.actor.{ Props, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import fr.inria.powerapi.core.{ Message, MessagesToListen }
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import org.junit.Test
import org.scalatest.junit.{ ShouldMatchersForJUnit, JUnitSuite }

class CpuFormulaMock extends CpuFormula {
  def process(cpuSensorMessage: CpuSensorMessage) {}
}

class CpuFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {
  lazy val system = ActorSystem("CpuFormulaSuite")
  implicit lazy val timeout = Timeout(5 seconds)

  @Test
  def testMessagesToListen() {
    val cpuFormula = system.actorOf(Props[CpuFormulaMock], name = "cpuFormulaMock")
    val request = cpuFormula ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 1
    messages(0) should be(classOf[CpuSensorMessage])
  }
}