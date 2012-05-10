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
package powerapi.sensor.cpusensor.linux
import java.io.FileInputStream
import java.io.IOException
import java.net.URL

import powerapi.core.Configuration
import powerapi.core.Process
import powerapi.core.Tick
import powerapi.sensor.cpusensor.CpuSensorValues
import powerapi.sensor.cpusensor.GlobalElapsedTime
import powerapi.sensor.cpusensor.ProcessElapsedTime
import powerapi.sensor.cpusensor.TimeInStates
import scalax.io.Resource

class CpuSensor extends powerapi.sensor.cpusensor.CpuSensor with Configuration {

  class Frequency {
    lazy val timeInStateFiles = {
      val timesInState = fromConf("timesInState")(elt => (elt \\ "@url").text)(0)
      val cores = fromConf("cores")(elt => (elt \\ "@value").text.toInt)(0)
      for (core <- 0 until cores) yield (timesInState replace ("%?", core.toString))
    }
    lazy val TimeInStateFormat = """(\d+)\s+(\d+)""".r

    def timeInStates = {
      val result = collection.mutable.HashMap[Int, Int]()

      timeInStateFiles.foreach(timeInStateFile => {
        try {
          // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
          // Then, we simply read these files thanks to a FileInputStream in getting those local path
          Resource.fromInputStream(new FileInputStream(new URL(timeInStateFile).getPath())).lines().foreach(line => {
            line match {
              case TimeInStateFormat(frequency, time) => result += (frequency.toInt -> (time.toInt + (result getOrElse (frequency.toInt, 0))))
              case _ => log.warning("unable to parse line \"" + line + "\" from file \"" + timeInStateFile)
            }
          })
        } catch {
          case ioe: IOException => {
            log.warning("i/o exception: " + ioe.getMessage)
          }
        }
      })

      result.toMap[Int, Int]
    }
  }

  class Time {
    lazy val globalStatFile = fromConf("globalStat")(elt => (elt \\ "@url").text)(0)
    lazy val GlobalStatFormat = """cpu\s+([\d\s]+)""".r
    def globalElapsedTime = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        Resource.fromInputStream(new FileInputStream(new URL(globalStatFile).getPath())).lines().toIndexedSeq(0) match {
          case GlobalStatFormat(times) => times.split(' ').foldLeft(0: Int) { (acc, x) => (acc + x.toInt) }
          case _ => {
            log.warning("unable to parse line from file \"" + globalStatFile)
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

    lazy val processStatFile = fromConf("processStat")(elt => (elt \\ "@url").text)(0)
    def processElapsedTime(implicit process: Process) = {
      try {
        // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
        // Then, we simply read these files thanks to a FileInputStream in getting those local path
        val line = Resource.fromInputStream(new FileInputStream(new URL(processStatFile replace ("%?", process.pid.toString)).getPath())).lines().toIndexedSeq(0) split ("\\s")
        // User time + System time + Block IO waiting time
        line(13).toInt + line(14).toInt + line(41).toInt
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

  def publish(sensorValues: CpuSensorValues) {
    context.system.eventStream publish sensorValues
  }

  def process(tick: Tick) {
    publish(
      CpuSensorValues(
        TimeInStates(timeInStates),
        GlobalElapsedTime(elapsedTime),
        ProcessElapsedTime(elapsedTime(tick.subscription.process)),
        tick))
  }
}
