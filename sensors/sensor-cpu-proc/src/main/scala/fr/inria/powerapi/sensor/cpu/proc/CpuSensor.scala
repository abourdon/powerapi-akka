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
package fr.inria.powerapi.sensor.cpu.proc

import java.io.FileInputStream
import java.io.IOException
import java.net.URL

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.ProcessPercent
import scalax.io.Resource

/**
 * CPU sensor configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * Global stat file, giving global information of the system itself.
   * Typically presents under /proc/stat.
   */
  lazy val globalStatPath = load { _.getString("powerapi.cpu.global-stat") }("file:///proc/stat")

  /**
   * Process stat file, giving information about the process itself.
   * Typically presents under /proc/[pid]/stat.
   */
  lazy val processStatPath = load { _.getString("powerapi.cpu.process-stat") }("file:///proc/%?/stat")
}

/**
 * CPU sensor component that collects data from a /proc and /sys directories
 * which are typically presents under a Linux platform.
 *
 * @see http://www.kernel.org/doc/man-pages/online/pages/man5/proc.5.html
 *
 * @author abourdon
 */
class CpuSensor extends fr.inria.powerapi.sensor.cpu.api.CpuSensor with Configuration {

  /**
   * Delegate class collecting time information contained into both globalStatPath and processStatPath files
   * and providing the process CPU percent usage.
   */
  class ProcessPercent {
    lazy val GlobalStatFormat = """cpu\s+([\d\s]+)""".r
    def globalElapsedTime: Long = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        Resource.fromInputStream(new FileInputStream(new URL(globalStatPath).getPath)).lines().toIndexedSeq(0) match {
          case GlobalStatFormat(times) => times.split(' ').foldLeft(0: Long) {
            (acc, x) => (acc + x.toLong)
          }
          case _ => {
            if (log.isWarningEnabled) log.warning("unable to parse line from file \"" + globalStatPath)
            0l
          }
        }
      } catch {
        case ioe: IOException =>
          if (log.isWarningEnabled) log.warning("i/o exception: " + ioe.getMessage)
          0l
      }
    }

    def processElapsedTime(implicit process: Process): Long = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        val line = Resource.fromInputStream(new FileInputStream(new URL(processStatPath replace ("%?", process.pid.toString)).getPath)).lines().toIndexedSeq(0).toString.split("\\s")
        // User time + System time
        line(13).toLong + line(14).toLong
      } catch {
        case ioe: IOException => {
          if (log.isWarningEnabled) log.warning("i/o exception: " + ioe.getMessage)
          0l
        }
      }
    }

    lazy val cache = collection.mutable.Map[TickSubscription, (Long, Long)]()
    def refrechCache(subscription: TickSubscription, now: (Long, Long)) {
      cache += (subscription -> now)
    }

    def process(subscription: TickSubscription) = {
      val now = (processElapsedTime(subscription.process), globalElapsedTime)
      val old = cache.getOrElse(subscription, now)
      refrechCache(subscription, now)

      val globalDiff = now._2 - old._2
      if (globalDiff == 0) {
        ProcessPercent(0)
      } else {
        ProcessPercent((now._1 - old._1).doubleValue() / globalDiff)
      }

    }
  }

  lazy val processPercent = new ProcessPercent

  def process(tick: Tick) {
    publish(
      CpuSensorMessage(
        processPercent = processPercent.process(tick.subscription),
        tick = tick))
  }
}