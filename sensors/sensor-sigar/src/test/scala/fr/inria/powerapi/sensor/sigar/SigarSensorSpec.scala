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
package fr.inria.powerapi.sensor.sigar

import org.scalatest.FlatSpec
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import fr.inria.powerapi.core.Tick
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class SigarSensorMock extends SigarSensor {
  def process(tick: Tick) {}
}

@RunWith(classOf[JUnitRunner])
class SigarSensorSpec extends FlatSpec with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("sensor-sigar")
  val sensor = TestActorRef[SigarSensorMock]

  "Initializer" should "define the java.library.path" in {
    sensor.underlyingActor.init should equal(true)
    System.getProperty("java.library.path") should not be null
  }

}