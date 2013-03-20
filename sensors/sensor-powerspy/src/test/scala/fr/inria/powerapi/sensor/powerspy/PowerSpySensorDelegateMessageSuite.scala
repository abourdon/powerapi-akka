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

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

class PowerSpySensorDelegateMessageSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Test
  def testSum() {
    PowerSpySensorDelegateMessage(1.0, 1.0f, 1.0f) + PowerSpySensorDelegateMessage(1.0, 1.0f, 1.0f) should equal(PowerSpySensorDelegateMessage(1.0 + 1.0, 1.0f + 1.0f, 1.0f + 1.0f))
  }

  @Test
  def testDivide() {
    PowerSpySensorDelegateMessage(3.0, 3.0f, 3.0f) / 2 should equal(PowerSpySensorDelegateMessage(3.0 / 2, 3.0f / 2, 3.0f / 2))
  }

  @Test
  def testAvg() {
    PowerSpySensorDelegateMessage.avg(PowerSpySensorDelegateMessage(1.0, 1.0f, 1.0f) :: PowerSpySensorDelegateMessage(2.0, 2.0f, 2.0f) :: PowerSpySensorDelegateMessage(3.0, 3.0f, 3.0f) :: Nil) should equal(PowerSpySensorDelegateMessage((1.0 + 2.0 + 3.0) / 3, (1.0f + 2.0f + 3.0f) / 3, (1.0f + 2.0f + 3.0f) / 3))
  }
}