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
package fr.inria.powerapi.sensor.cpu.sigar

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.FlatSpec

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process

@RunWith(classOf[JUnitRunner])
class CpuSensorSpec extends FlatSpec with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("cpu-sensor-sigar")
  val sensor = TestActorRef[CpuSensor]

  "Initializer" should "define the java.library.path" in {
    sensor.underlyingActor.init should equal(true)
    System.getProperty("java.library.path") should not be null
  }

  "Time by frequencies data structure" should "be empty" in {
    sensor.underlyingActor.timeInStates should have size 0
  }

  "Process elapsed time" should "increase as the duration time" in {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    val old = sensor.underlyingActor.elapsedTime(Process(currentPid))
    Thread.sleep((1 second).toMillis)
    val now = sensor.underlyingActor.elapsedTime(Process(currentPid))

    now should be >= old
  }

  "Global elapsed time" should "increase as the duration time" in {
    val old = sensor.underlyingActor.elapsedTime
    Thread.sleep((1 second).toMillis)
    val now = sensor.underlyingActor.elapsedTime

    now should be >= old
  }

}
