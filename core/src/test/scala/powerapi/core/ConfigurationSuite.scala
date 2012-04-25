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
package powerapi.core
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

class ConfigurationSuite extends JUnitSuite with ShouldMatchersForJUnit with Configuration {

  @Test
  def testKeyFromConf {
    val result = fromConf[String]("key") { elt => (elt \\ "@value").text }
    result should have size (1)
    result(0) should equal("value")
  }

  @Test
  def testStringsFromConf {
    val result = fromConf[String]("string") { elt => (elt \\ "@value").text }
    result should have size (3)
    result(0) should equal("string1")
    result(1) should equal("string2")
    result(2) should equal("string3")
  }

  @Test
  def testIntsFromConf {
    val result = fromConf[Int]("int") { elt => (elt \\ "@value").text.toInt }
    result.reduceLeft((acc, x) => acc + x) should equal(6)
  }

  @Test
  def testItemsFromConf {
    case class Item(id: Int, value: Double)
    val result = fromConf[Item]("item") { elt => Item((elt \\ "@id").text.toInt, (elt \\ "@value").text.toDouble) }
    result should have size (2)
    result(0) should equal(Item(1, 1.5))
    result(1) should equal(Item(2, 2.0))
  }

}