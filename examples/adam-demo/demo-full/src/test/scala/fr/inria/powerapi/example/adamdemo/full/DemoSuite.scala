/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerAPI.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.example.adamdemo.full

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.util.duration.intToDurationInt
import org.junit.Ignore

class DemoSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Ignore
  @Test
  def testDemos() {
    Array[Scenario](Demo.demo1, Demo.demo2, Demo.demo3, Demo.demo4, Demo.demo5).foreach(demo => {
      println("Running demonstration: \"" + demo.name + "\"")
      if (demo.init) {
        demo.start()
        Thread.sleep((15 seconds).toMillis)
        demo.stop()
      } else {
        fail("Initialization error")
      }
    })
  }

  @Ignore
  @Test
  def testSetProcessForOneProcessScenario() {
    Runtime.getRuntime().exec("firefox")
    Thread.sleep((5 seconds).toMillis)
    if (!Demo.demo1.init) {
      fail("Initialization error")
    }
    Demo.demo1.start()
    Thread.sleep((15 seconds).toMillis)

    Runtime.getRuntime().exec(Array("stress", "-c", "1"))
    Thread.sleep((5 seconds).toMillis)

    Demo.demo1.setProcess("stress")
    Thread.sleep((15 seconds).toMillis)
    Demo.demo1.stop()
  }

  @Ignore
  @Test
  def testSetProcessForGranularityScenario() {
    Runtime.getRuntime().exec("firefox")
    Thread.sleep((5 seconds).toMillis)
    if (!Demo.demo3.init) {
      fail("Initialization error")
    }
    Demo.demo3.start()
    Thread.sleep((15 seconds).toMillis)

    Runtime.getRuntime().exec(Array("stress", "-c", "1"))
    Thread.sleep((5 seconds).toMillis)

    Demo.demo3.setProcess("stress")
    Thread.sleep((15 seconds).toMillis)
    Demo.demo3.stop()
  }

}