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
package fr.inria.powerapi.reporter.console

import scala.concurrent.duration.DurationInt

import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.formula.cpu.max.CpuFormula
import fr.inria.powerapi.formula.mem.single.MemFormula
import fr.inria.powerapi.sensor.cpu.sigar.CpuSensor
import fr.inria.powerapi.sensor.mem.sigar.MemSensor
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import java.lang.management.ManagementFactory
import scalax.file.Path
import org.scalatest.junit.JUnitSuite
import org.junit.After
import org.junit.Before
import org.junit.Test
import fr.inria.powerapi.processor.aggregator.device.DeviceAggregator

class ConsoleReporterTest extends JUnitSuite with ShouldMatchersForJUnit {

  @Before
  def setUp() {
    Array(
      classOf[CpuSensor],
      classOf[CpuFormula],
      classOf[MemSensor],
      classOf[MemFormula]).foreach(PowerAPI.startEnergyModule(_))
  }

  @Test
  def testRun() {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(
      process = Process(currentPid),
      duration = 1.second,
      processor = classOf[DeviceAggregator],
      listener = classOf[ConsoleReporter])
    Thread.sleep((10.seconds).toMillis)
    PowerAPI.stopMonitoring(
      process = Process(currentPid),
      duration = 1.second,
      processor = classOf[DeviceAggregator],
      listener = classOf[ConsoleReporter])
  }

  @After
  def tearDown() {
    Array(
      classOf[CpuSensor],
      classOf[CpuFormula],
      classOf[MemSensor],
      classOf[MemFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

}