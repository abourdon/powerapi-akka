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
package fr.inria.powerapi.listener.disk.console
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import fr.inria.powerapi.formula.disk.single.DiskFormula
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.sensor.disk.proc.DiskSensor
import fr.inria.powerapi.core.Process
import akka.util.duration._
import org.junit.Before
import org.junit.After
import scalax.io.Resource
import org.junit.Ignore

class DiskListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Before
  def setup() {
    Array(classOf[DiskSensor], classOf[DiskFormula]).foreach(PowerAPI.startEnergyModule(_))
  }

  @After
  def tearDown() {
    Array(classOf[DiskSensor], classOf[DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

  @Ignore
  @Test
  def testPid {
    PowerAPI.startMonitoring(process = Process(31658), duration = 5 seconds, listener = classOf[DiskListener])
    Thread.sleep((10 minutes).toMillis)
    PowerAPI.stopMonitoring(process = Process(31658), duration = 5 seconds, listener = classOf[DiskListener])
  }

  @Test
  def testCurrentPid {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(process = Process(currentPid), duration = 500 milliseconds, listener = classOf[DiskListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(process = Process(currentPid), duration = 500 milliseconds, listener = classOf[DiskListener])
  }

  @Ignore
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

    PowerAPI.startMonitoring(listener = classOf[DiskListener])
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 500 milliseconds))

    Thread.sleep((10 seconds).toMillis)

    PowerAPI.stopMonitoring(listener = classOf[DiskListener])
    pids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = 500 milliseconds))
  }
}