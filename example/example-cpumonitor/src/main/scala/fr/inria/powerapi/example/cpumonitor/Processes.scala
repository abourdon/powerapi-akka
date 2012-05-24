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
package fr.inria.powerapi.example.cpumonitor
import java.lang.management.ManagementFactory
import com.typesafe.config.ConfigFactory
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.listener.cpu.jfreechart.CpuListener
import scalax.io.Resource
import scala.collection.JavaConversions

object Processes {
  lazy val conf = ConfigFactory.load

  def fromConf {
    val pids = JavaConversions.asScalaBuffer(conf.getIntList("powerapi.pids")).toList
    pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))
  }

  def perso {
    PowerAPI.startMonitoring(Process(16617), 500 milliseconds, classOf[CpuListener])
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(Process(16617), 500 milliseconds, classOf[CpuListener])
  }

  def current {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListener])
  }

  def intensive {
    def getPids = {
      val PSFormat = """^\s*(\d+).*""".r
      Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).lines().toList.map({ pid =>
        pid match {
          case PSFormat(pid) => pid.toInt
          case _ => 1
        }
      })
    }

    val pids = scala.collection.mutable.Set[Int]()
    val duration = 500 milliseconds
    def udpateMonitoredPids {
      val currentPids = scala.collection.mutable.Set[Int](getPids: _*)

      val oldPids = pids -- currentPids
      oldPids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), duration))
      pids --= oldPids

      val newPids = currentPids -- pids
      newPids.foreach(pid => PowerAPI.startMonitoring(Process(pid), duration, classOf[GatheredChart]))
      pids ++= newPids
    }

    val startingTime = System.currentTimeMillis
    while (System.currentTimeMillis - startingTime < (1 hour).toMillis) {
      udpateMonitoredPids
      Thread.sleep((250 milliseconds).toMillis)
    }
    PowerAPI.stopMonitoring(classOf[GatheredChart])
  }

}

