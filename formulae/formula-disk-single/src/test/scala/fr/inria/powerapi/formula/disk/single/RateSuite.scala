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

class RateSuiteWrapper extends Configuration with JUnitSuite with ShouldMatchersForJUnit {
  val megaByte = 1000000.0
  val gigaByte = 1000000000.0

  def messagesToListen = null
  def acquire = null

  def testFromRateToDoubleSimpleNumber() {
    "1GB/s".fromRateToDouble should equal(1 * gigaByte)
  }

  def testFromRateToDoubleRealNumberWithDot() {
    "2.3GB/s".fromRateToDouble should equal(2.3 * gigaByte)
  }

  def testFromRateToDoubleRealNumberWithComma() {
    "2,3GB/s".fromRateToDouble should equal(2.3 * gigaByte)
  }

  def testFromRateToDoubleWithMegaMultiplier() {
    "2,3MB/s".fromRateToDouble should equal(2.3 * megaByte)
  }
}

class RateSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("RateSuite")
  val rateSuite = TestActorRef[RateSuiteWrapper].underlyingActor

  @Test
  def testFromRateToDoubleSimpleNumber() {
    rateSuite.testFromRateToDoubleSimpleNumber()
  }

  @Test
  def testFromRateToDoubleRealNumberWithDot() {
    rateSuite.testFromRateToDoubleRealNumberWithDot()
  }

  @Test
  def testFromRateToDoubleRealNumberWithComma() {
    rateSuite.testFromRateToDoubleRealNumberWithComma()
  }

  @Test
  def testFromRateToDoubleWithMegaMultiplier() {
    rateSuite.testFromRateToDoubleWithMegaMultiplier()
  }
}