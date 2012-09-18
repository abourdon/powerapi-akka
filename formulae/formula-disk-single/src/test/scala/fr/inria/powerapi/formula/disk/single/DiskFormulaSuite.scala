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
package fr.inria.powerapi.formula.disk.single
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration._
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.sensor.disk.api.DiskSensorValues
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.core.Energy

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
    val old = DiskSensorValues(rw = Map("n/a" -> (123: Long, 456: Long)), Tick(TickSubscription(Process(123), 500 milliseconds)))
    diskFormula.refreshCache(old)
    diskFormula.cache getOrElse (TickSubscription(Process(123), 500 milliseconds), null) should equal(old)
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
    val duration = 500 milliseconds
    val old = DiskSensorValues(rw = Map("n/a" -> (100: Long, 200: Long)), Tick(TickSubscription(Process(123), duration)))
    val now = DiskSensorValues(rw = Map("n/a" -> (500: Long, 400: Long)), Tick(TickSubscription(Process(123), duration)))

    val durationToSeconds = duration.toMillis / 1000.0
    diskFormula.power(now, old) should equal(Energy.fromPower(((500 - 100) * diskFormula.readEnergyByByte / durationToSeconds + (400 - 200) * diskFormula.writeEnergyByByte / durationToSeconds) / 2.0))
  }

  @Test
  def testCompute() {
    val duration = 500 milliseconds
    val tick = Tick(TickSubscription(Process(123), duration))

    val old = DiskSensorValues(rw = Map("n/a" -> (100: Long, 200: Long)), tick)
    diskFormula.refreshCache(old)

    val now = DiskSensorValues(rw = Map("n/a" -> (500: Long, 400: Long)), tick)
    val durationToSeconds = duration.toMillis / 1000.0
    diskFormula.compute(now) should equal(DiskFormulaValues(Energy.fromPower(((500 - 100) * diskFormula.readEnergyByByte / durationToSeconds + (400 - 200) * diskFormula.writeEnergyByByte / durationToSeconds) / 2.0), tick))
  }
}