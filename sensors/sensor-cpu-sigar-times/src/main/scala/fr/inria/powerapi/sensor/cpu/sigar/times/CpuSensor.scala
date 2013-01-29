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
package fr.inria.powerapi.sensor.cpu.sigar.times

import java.io.FileInputStream
import java.io.IOException
import java.net.URL

import scala.collection.mutable

import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.ProcessPercent
import fr.inria.powerapi.sensor.cpu.api.TimeInStates
import scalax.io.Resource

/**
 * CPU sensor configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * Time in state file, giving information about how many time CPU spent under each frequency.
   * This information is typically given by the cpufrequtils utils.
   *
   * @see http://www.kernel.org/pub/linux/utils/kernel/cpufreq/cpufreq-info.html
   */
  lazy val timeInStatePath = load { _.getString("powerapi.cpu.time-in-state") }("file:///sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state")
}

/**
 * CPU sensor component that collects data from a /proc and /sys directories
 * which are typically presents under a Linux platform.
 *
 * @see http://www.kernel.org/doc/man-pages/online/pages/man5/proc.5.html
 *
 * @author abourdon
 */
class CpuSensor extends fr.inria.powerapi.sensor.cpu.sigar.CpuSensor with Configuration {

  /**
   * Delegate class to deal with time spent within each CPU frequencies.
   */
  class Frequencies {
    // time_in_state line format: frequency time
    lazy val TimeInStateFormat = """(\d+)\s+(\d+)""".r
    def timeInStates = {
      val result = collection.mutable.HashMap[Int, Long]()

      (for (core <- 0 until cores) yield (timeInStatePath replace ("%?", core.toString))).foreach(timeInStateFile => {
        try {
          // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
          // Then, we simply read these files thanks to a FileInputStream in getting those local path
          Resource.fromInputStream(new FileInputStream(new URL(timeInStateFile).getPath)).lines().foreach(f = line => {
            line match {
              case TimeInStateFormat(freq, t) => result += (freq.toInt -> (t.toLong + (result getOrElse (freq.toInt, 0: Long))))
              case _ => if (log.isWarningEnabled) log.warning("unable to parse line \"" + line + "\" from file \"" + timeInStateFile)
            }
          })
        } catch {
          case ioe: IOException => {
            if (log.isWarningEnabled) log.warning("i/o exception: " + ioe.getMessage)
          }
        }
      })

      result.toMap[Int, Long]
    }

    lazy val cache = mutable.HashMap[TickSubscription, TimeInStates]()
    def refreshCache(subscription: TickSubscription, now: TimeInStates) {
      cache += (subscription -> now)
    }

    def process(subscription: TickSubscription) = {
      val now = TimeInStates(timeInStates)
      val old = cache getOrElse (subscription, now)
      refreshCache(subscription, now)
      now - old
    }
  }

  lazy val frequencies = new Frequencies

  override def process(tick: Tick) {
    publish(
      CpuSensorMessage(
        timeInStates = frequencies.process(tick.subscription),
        processPercent = processPercent(tick.subscription.process),
        tick = tick
      )
    )
  }
}