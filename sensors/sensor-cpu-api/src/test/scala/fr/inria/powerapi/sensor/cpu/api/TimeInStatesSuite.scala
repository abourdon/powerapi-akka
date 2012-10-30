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
package fr.inria.powerapi.sensor.cpu.api

import org.junit.Test
import org.scalatest.junit.{ShouldMatchersForJUnit, JUnitSuite}

class TimeInStatesSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Test
  def testDifference() {
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