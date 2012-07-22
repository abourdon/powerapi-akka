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
package fr.inria.powerapi.core

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import com.typesafe.config.Config
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import scala.collection.JavaConversions

case class Item(id: Int, value: Double)
class ConfigurationMock extends Configuration {
  lazy val key = load { _.getString("powerapi.key") }("error")

  lazy val strings = for (
    config <- JavaConversions.asScalaBuffer(load { _.getConfigList("powerapi.strings") }(new java.util.ArrayList()))
  ) yield (config.asInstanceOf[Config].getString("string"))

  lazy val ints = for (
    config <- JavaConversions.asScalaBuffer(load { _.getConfigList("powerapi.ints") }(new java.util.ArrayList()))
  ) yield (config.asInstanceOf[Config].getInt("int"))

  lazy val items = for (
    config <- JavaConversions.asScalaBuffer(load { _.getConfigList("powerapi.items") }(new java.util.ArrayList()))
  ) yield (Item(config.asInstanceOf[Config].getInt("id"), config.asInstanceOf[Config].getDouble("value")))

  lazy val notFound = load { _.getBoolean("not-found") || true }(false)

  def messagesToListen = Array()

  def process = {
    case _ => ()
  }
}

class ConfigurationSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("configuration-suite")
  val configuration = TestActorRef[ConfigurationMock].underlyingActor

  @Test
  def testKeyFromConf() {
    configuration.key should equal("value")
  }

  @Test
  def testStringsFromConf() {
    configuration.strings should have size (3)
    configuration.strings(0) should equal("string1")
    configuration.strings(1) should equal("string2")
    configuration.strings(2) should equal("string3")
  }

  @Test
  def testIntsFromConf() {
    configuration.ints.reduceLeft((acc, x) => acc + x) should equal(6)
  }

  @Test
  def testItemsFromConf() {
    configuration.items should have size (2)
    configuration.items(0) should equal(Item(1, 1.5))
    configuration.items(1) should equal(Item(2, 2.0))
  }

  @Test
  def testNotFound() {
    configuration.notFound should be(false)
  }
}