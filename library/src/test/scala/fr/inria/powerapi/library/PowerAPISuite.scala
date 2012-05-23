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
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.library
import java.lang.management.ManagementFactory

import akka.actor.ActorLogging
import akka.util.duration._

import org.junit.Test
import org.scalatest.junit.{ ShouldMatchersForJUnit, JUnitSuite }

import fr.inria.powerapi.core.{ Listener, Clock, Process }
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor

class SimpleCpuListener extends Listener with ActorLogging {
  def process = {
    case values: CpuFormulaValues => println(values)
  }

  def messagesToListen = Array(classOf[CpuFormulaValues])
}

class PowerAPISuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testPowerAPI {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))

    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[SimpleCpuListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[SimpleCpuListener])

    PowerAPI.stopModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }
}