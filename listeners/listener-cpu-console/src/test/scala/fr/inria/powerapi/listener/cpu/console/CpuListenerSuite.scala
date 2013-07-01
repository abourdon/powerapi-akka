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
package fr.inria.powerapi.listener.cpu.console

import scala.concurrent.duration.DurationInt

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import java.lang.management.ManagementFactory
import org.junit.{ Test, Ignore, Before, After }
import org.scalatest.junit.{ ShouldMatchersForJUnit, JUnitSuite }
import scalax.io.Resource
import fr.inria.powerapi.formula.cpu.max.CpuFormula

class CpuListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Before
  def setUp() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))
  }

  @Test
  def testAllPids() {
    val PSFormat = """^\s*(\d+).*""".r
    val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case PSFormat(id) => id.toInt
          case _ => 1
        }
    })

    PowerAPI.startMonitoring(listener = classOf[CpuListener])
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 500.milliseconds))

    Thread.sleep((10.seconds).toMillis)

    PowerAPI.stopMonitoring(listener = classOf[CpuListener])
    pids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 500.milliseconds))
  }

  @Ignore
  @Test
  def testCurrentPid() {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(process = Process(currentPid), duration = 500.milliseconds, listener = classOf[CpuListener])
    Thread.sleep((5.minutes).toMillis)
    PowerAPI.stopMonitoring(process = Process(currentPid), duration = 500.milliseconds, listener = classOf[CpuListener])
  }

  @After
  def tearDown() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }
}