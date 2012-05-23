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
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.core

import akka.util.duration.intToDurationInt

import org.junit.Test
import org.scalatest.junit.{ShouldMatchersForJUnit, JUnitSuite}

class EnergySuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testFromPower {
    Energy.fromPower(3).power should equal(3)
  }

  @Test
  def testFromJoule {
    Energy.fromJoule(15).power should equal(15)
    Energy.fromJoule(15, 3 seconds).power should equal(5)
  }

  @Test
  def testAddition {
    (Energy.fromPower(3) + Energy.fromPower(7)) should equal(Energy.fromPower(10))
  }

  @Test
  def testMkString {
    Energy.fromPower(3).mkString should equal((3.0).toString)
  }

}