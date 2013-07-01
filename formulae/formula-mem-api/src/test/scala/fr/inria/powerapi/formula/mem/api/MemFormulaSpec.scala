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
package fr.inria.powerapi.formula.mem.api

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.FlatSpec
import akka.actor.ActorSystem
import akka.util.Timeout
import fr.inria.powerapi.sensor.mem.api.MemSensorMessage
import akka.actor.Props
import akka.pattern.ask
import fr.inria.powerapi.core.MessagesToListen
import fr.inria.powerapi.core.Message

@RunWith(classOf[JUnitRunner])
class MemFormulaSpec extends FlatSpec with ShouldMatchersForJUnit {

  lazy val system = ActorSystem("mem-formula-suite")
  implicit lazy val timeout = Timeout(5.seconds)

  "A MemFormula" should "react to MemSensorMessage" in {
    val memFormula = system.actorOf(Props(new MemFormula {
      def process(memSensorMessage: MemSensorMessage) {}
    }))
    val request = memFormula ? MessagesToListen
    val messages = Await.result(request, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]

    messages should have size 1
    messages(0) should be(classOf[MemSensorMessage])
  }

}