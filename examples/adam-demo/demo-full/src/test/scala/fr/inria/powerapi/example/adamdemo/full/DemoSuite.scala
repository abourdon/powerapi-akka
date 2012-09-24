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
package fr.inria.powerapi.example.adamdemo.full

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.util.duration.intToDurationInt
import org.junit.Ignore

class DemoSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testDemos {
    Array[Scenario](Demo.demo1, Demo.demo2, Demo.demo3, Demo.demo4, Demo.demo5).foreach(demo => {
      println("Running demonstration: \"" + demo.name + "\"")
      if (demo.init()) {
        demo.start()
        Thread.sleep((15 seconds).toMillis)
        demo.stop()
      } else {
        fail("Initialization error")
      }
    })
  }

}