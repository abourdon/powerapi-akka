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
package fr.inria.powerapi.example.adamdemo.full

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class DemoListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {

  implicit val system = ActorSystem("DemoListenerSuite")

  @Test
  def testAdd() {
    val demoListener = TestActorRef[DemoListener].underlyingActor
    val timestamp = 123456789L

    demoListener.cache should have size 0

    demoListener.add(timestamp, 123, "cpu", 1)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 1))))

    demoListener.add(timestamp, 123, "cpu", 2)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 3))))

    demoListener.add(timestamp, 123, "disk", 1)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 3, "disk" -> 1))))

    demoListener.add(timestamp, 123, "disk", 2)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 3, "disk" -> 3))))

    demoListener.add(timestamp, 456, "cpu", 1)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 4, "disk" -> 3))))
  }

  @Test
  def testRemove() {
    val demoListener = TestActorRef[DemoListener].underlyingActor
    val timestamp = 123456789L
    val otherTimestamp = timestamp + 1

    demoListener.cache should have size 0

    demoListener.add(timestamp, 123, "cpu", 1)
    demoListener.cache should have size 1
    demoListener.cache should contain key timestamp

    demoListener.remove(otherTimestamp)
    demoListener.cache should have size 1
    demoListener.cache should contain key timestamp

    demoListener.remove(timestamp)
    demoListener.cache should have size 0

    demoListener.add(timestamp, 123, "cpu", 1)
    demoListener.add(otherTimestamp, 123, "cpu", 1)
    demoListener.cache should have size 2
    demoListener.cache should contain key timestamp
    demoListener.cache should contain key otherTimestamp

    demoListener.remove(timestamp)
    demoListener.cache should have size 1
    demoListener.cache should contain key otherTimestamp

    demoListener.remove(otherTimestamp)
    demoListener.cache should have size 0
  }

  @Test
  def testFlush() {
    val demoListener = TestActorRef[DemoListener].underlyingActor
    val timestamp = 123456789L
    val otherTimestamp = timestamp + 1

    demoListener.cache should have size 0

    demoListener.add(timestamp, 123, "cpu", 1)
    demoListener.flush(timestamp)
    demoListener.cache should have size 1
    demoListener.cache should contain key timestamp

    demoListener.add(otherTimestamp, 123, "cpu", 1)
    demoListener.flush(otherTimestamp)
    demoListener.cache should have size 1
    demoListener.cache should contain key otherTimestamp
  }

  @Test
  def testHighlightedPid() {
    val demoListener = TestActorRef[DemoListener].underlyingActor
    val timestamp = 123456789L

    demoListener.cache should have size 0

    demoListener.add(timestamp, 123, "cpu", 1)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 1))))

    DemoListener.pidName(456, "n/a")

    demoListener.add(timestamp, 456, "cpu", 1)
    demoListener.cache should equal(Map(timestamp -> Map(-1 -> Map("cpu" -> 1), 456 -> Map("cpu" -> 1))))
  }

}