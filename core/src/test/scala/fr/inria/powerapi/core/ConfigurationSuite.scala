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
package fr.inria.powerapi.core
import scala.collection.JavaConversions

import org.junit.Test
import org.scalatest.junit.{ ShouldMatchersForJUnit, JUnitSuite }

import com.typesafe.config.Config

class ConfigurationSuite extends JUnitSuite with ShouldMatchersForJUnit with Configuration {

  @Test
  def testKeyFromConf {
    conf.getString("powerapi.key") should equal("value")
  }

  @Test
  def testStringsFromConf {
    val strings = for (config <- JavaConversions.asScalaBuffer(conf.getConfigList("powerapi.strings"))) yield (config.asInstanceOf[Config].getString("string"))
    strings should have size (3)
    strings(0) should equal("string1")
    strings(1) should equal("string2")
    strings(2) should equal("string3")
  }

  @Test
  def testIntsFromConf {
    val ints = for (config <- JavaConversions.asScalaBuffer(conf.getConfigList("powerapi.ints"))) yield (config.asInstanceOf[Config].getInt("int"))
    ints.reduceLeft((acc, x) => acc + x) should equal(6)
  }

  @Test
  def testItemsFromConf {
    case class Item(id: Int, value: Double)
    val items = for (config <- JavaConversions.asScalaBuffer(conf.getConfigList("powerapi.items"))) yield (Item(config.asInstanceOf[Config].getInt("id"), config.asInstanceOf[Config].getDouble("value")))
    items should have size (2)
    items(0) should equal(Item(1, 1.5))
    items(1) should equal(Item(2, 2.0))
  }

}