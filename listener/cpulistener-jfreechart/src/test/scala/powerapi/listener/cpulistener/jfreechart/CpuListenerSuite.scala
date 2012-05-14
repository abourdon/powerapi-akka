/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
package powerapi.listener.cpulistener.jfreechart
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import powerapi.powerapi.PowerAPI
import java.lang.management.ManagementFactory
import powerapi.core.Clock
import powerapi.sensor.cpusensor.linux.CpuSensor
import powerapi.formula.cpuformula.simple.CpuFormula
import powerapi.core.Process
import akka.util.duration._
import org.junit.Before
import org.junit.After
import scala.io.Source
import org.junit.Ignore

class CpuListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Before
  def setUp {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }

  @Test
  def testCurrentPid {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
  }

  @Ignore
  @Test
  def testAllPids {
    val PSFormat = """^\s*(\d+).*""".r
    val pids = Source.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).getLines.toList.map({ pid =>
      pid match {
        case PSFormat(pid) => pid.toInt
        case _ => 1
      }
    })
    pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))
  }

  @After
  def tearDown {
    PowerAPI.stopModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }

}