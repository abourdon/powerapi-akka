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
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.GlobalElapsedTime
import fr.inria.powerapi.sensor.cpu.api.ProcessElapsedTime
import fr.inria.powerapi.sensor.cpu.api.TimeInStates
import scalax.io.Resource

/**
 * CPU sensor configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * Cores number.
   */
  lazy val cores = load { _.getInt("powerapi.cpu.cores") }(0)

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

  /**
   * Time in state file, giving information about how many time CPU spent under each frequency.
   * This information is typically given by the cpufrequtils utils.
   *
   * @see http://www.kernel.org/pub/linux/utils/kernel/cpufreq/cpufreq-info.html
   */
  lazy val timeInStatePath = load { _.getString("powerapi.cpu.time-in-state") }("file:///sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state")

  /**
   * [Optional] If the time in state feature is enabled or not. True as default.
   *
   * @see timeInStatePath
   */
  lazy val timeInStateEnabled = load({ _.getBoolean("powerapi.sensor.cpu-proc.time-in-state") }, required = false)(true)
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
   * Delegation class collecting frequency information contained into the timeInStatePath file
   */
  class Frequency {
    // time_in_state line format: frequency time
    lazy val TimeInStateFormat = """(\d+)\s+(\d+)""".r

    lazy val mockedTimeInStates = Map[Int, Long]()

    def timeInStates = {
      val result = collection.mutable.HashMap[Int, Long]()

      (for (core <- 0 until cores) yield (timeInStatePath replace ("%?", core.toString))).foreach(timeInStateFile => {
        try {
          // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
          // Then, we simply read these files thanks to a FileInputStream in getting those local path
          Resource.fromInputStream(new FileInputStream(new URL(timeInStateFile).getPath)).lines().foreach(f = line => {
            line match {
              case TimeInStateFormat(freq, t) => result += (freq.toInt -> (t.toLong + (result getOrElse (freq.toInt, 0: Long))))
              case _ => log.warning("unable to parse line \"" + line + "\" from file \"" + timeInStateFile)
            }
          })
        } catch {
          case ioe: IOException => {
            log.warning("i/o exception: " + ioe.getMessage)
          }
        }
      })

      result.toMap[Int, Long]
    }
  }

  /**
   * Delegation class collecting time information contained into both globalStatPath and processStatPath files
   */
  class Time {
    lazy val GlobalStatFormat = """cpu\s+([\d\s]+)""".r

    def globalElapsedTime = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        Resource.fromInputStream(new FileInputStream(new URL(globalStatPath).getPath)).lines().toIndexedSeq(0) match {
          case GlobalStatFormat(times) => times.split(' ').foldLeft(0: Long) {
            (acc, x) => (acc + x.toLong)
          }
          case _ => {
            log.warning("unable to parse line from file \"" + globalStatPath)
            0l
          }
        }
      } catch {
        case ioe: IOException =>
          log.warning("i/o exception: " + ioe.getMessage)
          0l
      }
    }

    def processElapsedTime(implicit process: Process) = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        val line = Resource.fromInputStream(new FileInputStream(new URL(processStatPath replace ("%?", process.pid.toString)).getPath)).lines().toIndexedSeq(0).toString.split("\\s")
        // User time + System time
        line(13).toLong + line(14).toLong
      } catch {
        case ioe: IOException => {
          log.warning("i/o exception: " + ioe.getMessage)
          0
        }
      }
    }

    def elapsedTime(implicit process: Process = Process(-1)) = {
      if (process == Process(-1))
        globalElapsedTime
      else
        processElapsedTime
    }
  }

  lazy val frequency = new Frequency

  def timeInStates = if (timeInStateEnabled) frequency.timeInStates else frequency.mockedTimeInStates

  lazy val time = new Time

  def elapsedTime(implicit process: Process = Process(-1)) = time.elapsedTime

  def process(tick: Tick) {
    publish(
      CpuSensorMessage(
        timeInStates = TimeInStates(timeInStates),
        globalElapsedTime = GlobalElapsedTime(elapsedTime),
        processElapsedTime = ProcessElapsedTime(elapsedTime(tick.subscription.process)),
        tick = tick
      )
    )
  }
}