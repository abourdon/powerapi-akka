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
package fr.inria.powerapi.library

import scala.concurrent.duration.DurationInt
import akka.actor.ActorLogging
import fr.inria.powerapi.core.{ Listener, Process }
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import java.lang.management.ManagementFactory
import org.junit.Test
import org.scalatest.junit.{ ShouldMatchersForJUnit, JUnitSuite }
import fr.inria.powerapi.formula.cpu.max.CpuFormula

class SimpleCpuListener extends Listener {
  def acquire = {
    case values: CpuFormulaMessage => println(values)
  }

  def messagesToListen = Array(classOf[CpuFormulaMessage])
}

class PowerAPISuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Test
  def testPowerAPI() {
    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))

    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(process = Process(currentPid), duration = 500.milliseconds, listener = classOf[SimpleCpuListener])
    Thread.sleep((10.seconds).toMillis)
    PowerAPI.stopMonitoring(process = Process(currentPid), duration = 500.milliseconds, listener = classOf[SimpleCpuListener])

    Array(classOf[CpuSensor], classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }
}