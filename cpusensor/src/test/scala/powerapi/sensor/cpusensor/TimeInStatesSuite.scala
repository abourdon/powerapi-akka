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
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

class TimeInStatesSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testDifference {
    val timesLeft = TimeInStates(Map(
      1 -> 10,
      2 -> 20,
      3 -> 30,
      4 -> 15))

    val timesRight = TimeInStates(Map(
      1 -> 1,
      2 -> 2,
      3 -> 3,
      100 -> 100))

    (timesLeft - timesRight) should equal(TimeInStates(Map(
      1 -> 9,
      2 -> 18,
      3 -> 27,
      4 -> 15)))
  }

}