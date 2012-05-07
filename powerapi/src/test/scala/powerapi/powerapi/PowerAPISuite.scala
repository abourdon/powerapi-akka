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
package powerapi.powerapi
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import powerapi.formula.cpuformula.CPUFormulaValues
import akka.actor.ActorLogging
import powerapi.core.Listener
import powerapi.core.Clock
import powerapi.core.Process
import akka.util.duration._
import powerapi.sensor.cpusensor.CPUSensor
import powerapi.formula.cpuformula.CPUFormula
import java.lang.management.ManagementFactory

class SimpleCPUListener extends Listener with ActorLogging {
  def listen = {
    case values: CPUFormulaValues => println(values)
  }

  def messagesToListen = Array(classOf[CPUFormulaValues])
}

class PowerAPISuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testPowerAPI {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CPUSensor], classOf[CPUFormula]))

    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[SimpleCPUListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[SimpleCPUListener])

    PowerAPI.stopModules(Array(classOf[Clock], classOf[CPUSensor], classOf[CPUFormula]))
  }
}