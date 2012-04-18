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
package powerapi.sensor.cpusensor
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import powerapi.formula.cpuformula.CPUFormula
import powerapi.formula.cpuformula.Frequency

class CPUFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("cpuformulasuite")
  val cpuformula = TestActorRef[CPUFormula].underlyingActor

  @Test
  def testTdp {
    cpuformula.tdp should equal(42)
  }

  @Test
  def testNumberOfCores {
    cpuformula.numberOfCores should equal(4)
  }

  @Test
  def testFrequencies {
    cpuformula.frequencies.size should equal(3)
    cpuformula.frequencies(0) should equal(Frequency(1800002, 1.31))
    cpuformula.frequencies(1) should equal(Frequency(2100002, 1.41))
    cpuformula.frequencies(2) should equal(Frequency(2400003, 1.5))
  }

}