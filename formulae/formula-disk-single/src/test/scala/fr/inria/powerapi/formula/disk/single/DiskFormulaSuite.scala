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

  val giga = 1000000000.0
  val byte = 8.0
  
  @Test
  def testReadPower() {
    diskFormula.readPower should equal(2.1)
  }

  @Test
  def testWritePower() {
    diskFormula.writePower should equal(2.2)
  }

  @Test
  def testMaxRate() {
    diskFormula.maxRate should equal(3 * giga / byte)
  }

  @Test
  def testRefreshCache() {
    val old = DiskSensorValues(rw = Map("n/a" -> (123: Long, 456: Long)), Tick(TickSubscription(Process(123), 500 milliseconds)))
    diskFormula.refreshCache(old)
    diskFormula.cache getOrElse (TickSubscription(Process(123), 500 milliseconds), null) should equal(old)
  }

  @Test
  def testReadPowerByByte() {
    diskFormula.readPowerByByte should equal(2.1 / (3 * giga / byte))
  }

  @Test
  def testWritePowerByByte() {
    diskFormula.writePowerByByte should equal(2.2 / (3 * giga / byte))
  }

  @Test
  def testPower() {
    val old = DiskSensorValues(rw = Map("n/a" -> (100: Long, 200: Long)), Tick(TickSubscription(Process(123), 500 milliseconds)))
    val now = DiskSensorValues(rw = Map("n/a" -> (500: Long, 400: Long)), Tick(TickSubscription(Process(123), 500 milliseconds)))
    diskFormula.power(now, old) should equal((500 - 100) * diskFormula.readPowerByByte + (400 - 200) * diskFormula.writePowerByByte)
  }

  @Test
  def testCompute() {
    val tick = Tick(TickSubscription(Process(123), 500 milliseconds))

    val old = DiskSensorValues(rw = Map("n/a" -> (100: Long, 200: Long)), tick)
    diskFormula.refreshCache(old)

    val now = DiskSensorValues(rw = Map("n/a" -> (500: Long, 400: Long)), tick)
    diskFormula.compute(now) should equal(DiskFormulaValues(Energy.fromPower((500 - 100) * diskFormula.readPowerByByte + (400 - 200) * diskFormula.writePowerByByte), tick))
  }
}