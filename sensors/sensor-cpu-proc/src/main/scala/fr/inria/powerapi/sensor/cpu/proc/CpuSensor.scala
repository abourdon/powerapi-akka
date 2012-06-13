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
package fr.inria.powerapi.sensor.cpu.proc

import java.io.{ IOException, FileInputStream }
import java.net.URL

import fr.inria.powerapi.core.{ Tick, Process }
import fr.inria.powerapi.sensor.cpu.api.{ TimeInStates, ProcessElapsedTime, GlobalElapsedTime, CpuSensorValues }
import scalax.io.Resource

/**
 * CPU sensor configuration.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val cores = load { _.getInt("powerapi.cpu.cores") }(0)
  lazy val globalStatPath = load { _.getString("powerapi.cpu.global-stat") }("file:///proc/stat")
  lazy val processStatPath = load { _.getString("powerapi.cpu.process-stat") }("file:///proc/%?/stat")
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
class CpuSensor extends fr.inria.powerapi.sensor.cpu.api.CpuSensor with Configuration {
  /**
   * Delegation class collecting frequency information contained into the timeInStatePath file
   */
  class Frequency {
    lazy val TimeInStateFormat = """(\d+)\s+(\d+)""".r
    def timeInStates = {
      val result = collection.mutable.HashMap[Int, Long]()

      (for (core <- 0 until cores) yield (timeInStatePath replace ("%?", core.toString))).foreach(timeInStateFile => {
        try {
          // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
          // Then, we simply read these files thanks to a FileInputStream in getting those local path
          Resource.fromInputStream(new FileInputStream(new URL(timeInStateFile).getPath())).lines().foreach(line => {
            line match {
              case TimeInStateFormat(frequency, time) => result += (frequency.toInt -> (time.toLong + (result getOrElse (frequency.toInt, 0: Long))))
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
        Resource.fromInputStream(new FileInputStream(new URL(globalStatPath).getPath())).lines().toIndexedSeq(0) match {
          case GlobalStatFormat(times) => times.split(' ').foldLeft(0: Long) { (acc, x) => (acc + x.toLong) }
          case _ => {
            log.warning("unable to parse line from file \"" + globalStatPath)
            0
          }
        }
      } catch {
        case ioe: IOException => {
          log.warning("i/o exception: " + ioe.getMessage)
          0
        }
      }
    }

    def processElapsedTime(implicit process: Process) = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        val line = Resource.fromInputStream(new FileInputStream(new URL(processStatPath replace ("%?", process.pid.toString)).getPath())).lines().toIndexedSeq(0) split ("\\s")
        // User time + System time + Block IO waiting time
        line(13).toLong + line(14).toLong + line(41).toLong
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
  def timeInStates = frequency.timeInStates

  lazy val time = new Time
  def elapsedTime(implicit process: Process = Process(-1)) = time.elapsedTime

  def process(tick: Tick) {
    publish(
      CpuSensorValues(
        TimeInStates(timeInStates),
        GlobalElapsedTime(elapsedTime),
        ProcessElapsedTime(elapsedTime(tick.subscription.process)),
        tick))
  }
}