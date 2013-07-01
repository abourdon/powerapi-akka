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
package fr.inria.powerapi.sensor.mem.proc

import java.net.URL

import scala.concurrent.duration.Duration
import scala.util.Properties

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.FlatSpec

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription

@RunWith(classOf[JUnitRunner])
class MemSensorSpec extends FlatSpec with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("sensor-mem-proc")

  trait ConfigurationMock extends Configuration {
    lazy val basedir = new URL("file", Properties.propOrEmpty("basedir"), "")
    override lazy val meminfoPath = new URL(basedir, "/src/test/resources/proc/meminfo").toString
    override lazy val statusPath = new URL(basedir, "/src/test/resources/proc/%?/status").toString
  }

  val sensor = TestActorRef(new MemSensor with ConfigurationMock)

  "MemSensor" should "reads total memory from the specific file" in {
    sensor.underlyingActor.resident.memTotal should equal (4045760)
  }

  "MemSensor" should "reads process resident size memory from the specific file" in {
    sensor.underlyingActor.resident.vmRSS(Process(123)) should equal (48788)
  }

  "MemSensor" should "computes the process resident size memory percent" in {
    sensor.underlyingActor.resident.perc(Tick(subscription = TickSubscription(Process(123), Duration.Zero))) should equal (48788.doubleValue / 4045760)
  }

}