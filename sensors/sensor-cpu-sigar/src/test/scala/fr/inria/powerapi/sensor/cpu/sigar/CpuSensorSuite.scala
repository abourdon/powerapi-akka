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

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process
import akka.actor.ActorLogging
import akka.actor.Actor

/**
 * Utility test class to use Akka actor logging
 */
class Logger extends Actor with ActorLogging {
  def receive = {
    case _ => {}
  }
}

class CpuSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("cpu-sensor-sigar")
  val sensor = TestActorRef[CpuSensor]
  val logger = TestActorRef[CpuSensor].underlyingActor
  lazy val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt

  @Test
  def testTimeInState() {
    sensor.underlyingActor.timeInStates should have size 0
  }

  @Test
  def testProcessElapsedTime() {
    val old = sensor.underlyingActor.elapsedTime(Process(currentPid))
    Thread.sleep((1 second).toMillis)
    val now = sensor.underlyingActor.elapsedTime(Process(currentPid))

    logger.log.info("old: " + old + ", now: " + now)
    now should be >= old
  }

  @Test
  def testGlobalElapsedTime() {
    val old = sensor.underlyingActor.elapsedTime
    Thread.sleep((1 second).toMillis)
    val now = sensor.underlyingActor.elapsedTime

    logger.log.info("old: " + old + ", now: " + now)
    now should be >= old
  }

}
