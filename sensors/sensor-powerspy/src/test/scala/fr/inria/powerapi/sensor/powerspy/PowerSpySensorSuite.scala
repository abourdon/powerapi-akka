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
package fr.inria.powerapi.sensor.powerspy

import scala.concurrent.duration.DurationInt

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import org.junit.Ignore

class PowerSpySensorListener extends Listener {
  def messagesToListen = Array(classOf[PowerSpySensorMessage])

  def process(powerSpySensorMessage: PowerSpySensorMessage) {
    println("Received " + powerSpySensorMessage)
  }

  def acquire = {
    case powerSpySensorMessage: PowerSpySensorMessage => process(powerSpySensorMessage)
  }
}

class PowerSpySensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  
  @Ignore
  @Test
  def testTick() {
    PowerAPI.startEnergyModule(classOf[PowerSpySensor])
    PowerAPI.startMonitoring(process = Process(1), duration = 500.milliseconds, listener = classOf[PowerSpySensorListener])

    Thread.sleep((30.seconds).toMillis)

    PowerAPI.stopMonitoring(process = Process(1), duration = 500.milliseconds, listener = classOf[PowerSpySensorListener])
    PowerAPI.stopEnergyModule(classOf[PowerSpySensor])
  }

}