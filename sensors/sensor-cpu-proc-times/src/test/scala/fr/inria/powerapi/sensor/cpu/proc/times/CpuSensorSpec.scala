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
package fr.inria.powerapi.sensor.cpu.proc.times
import java.net.URL

import scala.concurrent.duration.DurationInt
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
import fr.inria.powerapi.sensor.cpu.api.TimeInStates

@RunWith(classOf[JUnitRunner])
class CpuSensorSpec extends FlatSpec with ShouldMatchersForJUnit {

  trait ConfigurationMock extends Configuration {
    override lazy val cores = 4
    override lazy val timeInStatePath = new URL(
      new URL("file", Properties.propOrEmpty("basedir"), ""),
      "/src/test/resources/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state").toString
  }

  implicit val system = ActorSystem("cpusensorsuite")
  implicit val tick = Tick(TickSubscription(Process(123), 1.second))
  val cpuSensor = TestActorRef(new CpuSensor with ConfigurationMock)

  "Frequencies' time in states" should "be correctly read from the dedicated system file" in {
    cpuSensor.underlyingActor.frequencies.timeInStates should equal(Map(
      4000000 -> 16,
      3000000 -> 12,
      2000000 -> 8,
      1000000 -> 4
    ))
  }

  "Frequencies' cache" should "be correctly updated during process phase" in {
    cpuSensor.underlyingActor.frequencies.cache should have size 0
    cpuSensor.underlyingActor.frequencies.process(tick.subscription)
    cpuSensor.underlyingActor.frequencies.cache should equal(Map(tick.subscription -> TimeInStates(Map(
      4000000 -> 16,
      3000000 -> 12,
      2000000 -> 8,
      1000000 -> 4
    ))))
  }

}