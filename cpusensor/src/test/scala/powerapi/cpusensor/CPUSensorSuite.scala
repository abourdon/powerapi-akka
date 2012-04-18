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
package powerapi.cpusensor
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class CPUSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  case object Test

  implicit val system = ActorSystem("cpusensorsuite")
  val cpusensor = TestActorRef[CPUSensor].underlyingActor

  @Test
  def testTdp {
    cpusensor.tdp should equal(42)
  }

  @Test
  def testNumberOfCores {
    cpusensor.numberOfCores should equal(4)
  }

  @Test
  def testFrequencies {
    cpusensor.frequencies.size should equal(3)
    cpusensor.frequencies(0) should equal(Frequency(1800002, 1.31))
    cpusensor.frequencies(1) should equal(Frequency(2100002, 1.41))
    cpusensor.frequencies(2) should equal(Frequency(2400003, 1.5))
  }

}