/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.listener.cpu.jfreechart

import akka.util.duration._
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import java.lang.management.ManagementFactory
import org.junit.{Test, Ignore, Before, After}
import org.scalatest.junit.{ShouldMatchersForJUnit, JUnitSuite}
import scalax.io.Resource


class CpuListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Before
  def setUp() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))
  }

  @Test
  def testCurrentPid() {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
  }

  @Ignore
  @Test
  def testAllPids() {
    val PSFormat = """^\s*(\d+).*""".r
    val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-e", "rho", "pid")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case PSFormat(id) => id.toInt
          case _ => 1
        }
    })
    PowerAPI.startMonitoring(listenerType = classOf[CpuListener])
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 500 milliseconds))

    Thread.sleep((5 minutes).toMillis)

    PowerAPI.stopMonitoring(listenerType = classOf[CpuListener])
    pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))
  }

  @After
  def tearDown() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }
}