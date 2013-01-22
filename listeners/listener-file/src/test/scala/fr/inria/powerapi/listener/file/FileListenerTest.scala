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
package fr.inria.powerapi.listener.file

import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.formula.cpu.max.CpuFormula
import fr.inria.powerapi.formula.mem.single.MemFormula
import fr.inria.powerapi.sensor.cpu.sigar.CpuSensor
import fr.inria.powerapi.sensor.mem.sigar.MemSensor
import fr.inria.powerapi.core.Process
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.library.PowerAPI
import java.lang.management.ManagementFactory
import scalax.file.Path
import org.scalatest.junit.JUnitSuite
import org.junit.After
import org.junit.Before
import org.junit.Test
import fr.inria.powerapi.listener.aggregator.DeviceAggregator

trait FileListenerMock extends Configuration {
  override lazy val filePath = Path.createTempFile(prefix = "powerapi.listener.file.prefix", deleteOnExit = false).path
}

class FileListenerTest extends JUnitSuite with ShouldMatchersForJUnit {

  @Before
  def setUp() {
    Array(classOf[CpuSensor], classOf[CpuFormula], classOf[MemSensor], classOf[MemFormula]).foreach(PowerAPI.startEnergyModule(_))
    PowerAPI.startMonitoring(listener = classOf[DeviceAggregator])
  }

  @Test
  def testRun() {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(process = Process(currentPid), duration = 1 second, listener = classOf[FileListener])
    Thread.sleep((5 seconds).toMillis)
    PowerAPI.stopMonitoring(process = Process(currentPid), duration = 1 second, listener = classOf[FileListener])
  }

  @After
  def tearDown() {
    PowerAPI.stopMonitoring(listener = classOf[DeviceAggregator])
    Array(classOf[CpuSensor], classOf[CpuFormula], classOf[MemSensor], classOf[MemFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

}