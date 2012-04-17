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
import org.junit.Assert
import org.junit.Ignore

case class SimpleFrequency(frequency: Int, voltage: Double)

@Test
class ConfigurationTest extends Configuration {

  @Test
  def testKeyFromConf {
    val result = fromConf[String]("key") { elt => (elt \\ "@value").text }
    Assert.assertEquals(1, result.size)
    Assert.assertEquals("value", result(0))
  }

  @Test
  def testStringsFromConf {
    val result = fromConf[String]("string") { elt => (elt \\ "@value").text }
    Assert.assertEquals(3, result.size)
    Assert.assertEquals("string1", result(0))
    Assert.assertEquals("string2", result(1))
    Assert.assertEquals("string3", result(2))
  }

  @Test
  def testIntsFromConf {
    val result = fromConf[Int]("int") { elt => (elt \\ "@value").text.toInt }
    Assert.assertEquals(6, result.reduceLeft((acc, x) => acc + x))
  }

  @Test
  def testFrequenciesFromConf {
    val result = fromConf[SimpleFrequency]("frequency") { elt => SimpleFrequency((elt \\ "@value").text.toInt, (elt \\ "@voltage").text.toDouble) }
    Assert.assertEquals(2, result.size)
    Assert.assertEquals(SimpleFrequency(2000000, 1.5), result(0))
    Assert.assertEquals(SimpleFrequency(2500000, 2.0), result(1))
  }

}