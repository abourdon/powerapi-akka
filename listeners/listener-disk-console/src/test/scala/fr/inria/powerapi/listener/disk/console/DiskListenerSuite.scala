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
    PowerAPI.startMonitoring(Process(31658), 5 seconds, classOf[DiskListener])
    Thread.sleep((10 minutes).toMillis)
    PowerAPI.stopMonitoring(Process(31658), 5 seconds, classOf[DiskListener])
  }

  @Test
  def testCurrentPid {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[DiskListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[DiskListener])
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

    PowerAPI.startMonitoring(listenerType = classOf[DiskListener])
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 500 milliseconds))

    Thread.sleep((10 seconds).toMillis)

    PowerAPI.stopMonitoring(listenerType = classOf[DiskListener])
    pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[DiskListener]))
  }
}