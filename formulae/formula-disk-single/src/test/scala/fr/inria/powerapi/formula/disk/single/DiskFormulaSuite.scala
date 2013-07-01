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
package fr.inria.powerapi.formula.disk.single

import scala.concurrent.duration.DurationInt

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.formula.disk.api.DiskFormulaMessage
import fr.inria.powerapi.sensor.disk.api.DiskSensorMessage

class DiskFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("DiskFormulaSuiteSystem")
  val diskFormula = TestActorRef[DiskFormula].underlyingActor

  val megaByte = 1000000.0

  @Test
  def testReadPower() {
    diskFormula.readPower should equal(2.1)
  }

  @Test
  def testWritePower() {
    diskFormula.writePower should equal(2.2)
  }

  @Test
  def testReadRate() {
    diskFormula.readRate should equal(100 * megaByte)
  }

  @Test
  def testWriteRate() {
    diskFormula.writeRate should equal(90 * megaByte)
  }

  @Test
  def testRefreshCache() {
    val old = DiskSensorMessage(rw = Map("n/a" -> (123: Long, 456: Long)), Tick(TickSubscription(Process(123), 500.milliseconds)))
    diskFormula.refreshCache(old)
    diskFormula.cache getOrElse (TickSubscription(Process(123), 500.milliseconds), null) should equal(old)
  }

  @Test
  def testReadEnergyByByte() {
    diskFormula.readEnergyByByte should equal(2.1 / (100 * megaByte))
  }

  @Test
  def testWriteEnergyByByte() {
    diskFormula.writeEnergyByByte should equal(2.2 / (90 * megaByte))
  }

  @Test
  def testPower() {
    val duration = 500.milliseconds
    val old = DiskSensorMessage(rw = Map("n/a" -> (100: Long, 200: Long)), Tick(TickSubscription(Process(123), duration)))
    val now = DiskSensorMessage(rw = Map("n/a" -> (500: Long, 400: Long)), Tick(TickSubscription(Process(123), duration)))

    diskFormula.power(now, old) should equal(Energy.fromJoule(((500 - 100) * diskFormula.readEnergyByByte + (400 - 200) * diskFormula.writeEnergyByByte), duration))
  }

  @Test
  def testCompute() {
    val duration = 500.milliseconds
    val tick = Tick(TickSubscription(Process(123), duration))

    val old = DiskSensorMessage(rw = Map("n/a" -> (100: Long, 200: Long)), tick)
    diskFormula.refreshCache(old)

    val now = DiskSensorMessage(rw = Map("n/a" -> (500: Long, 400: Long)), tick)
    diskFormula.compute(now) should equal(DiskFormulaMessage(Energy.fromJoule(((500 - 100) * diskFormula.readEnergyByByte + (400 - 200) * diskFormula.writeEnergyByByte), duration), tick))
  }
}