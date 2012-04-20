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
package powerapi.sensor.cpusensor
import akka.actor.Actor
import powerapi.core.Configuration
import powerapi.core.Tick
import powerapi.core.Process
import scala.io.Source
import scala.util.matching.Regex
import akka.actor.ActorLogging
import scala.concurrent.ops
import akka.actor.Props
import java.io.File

/** Messages definition */
case class TimeInStates(times: Map[Int, Int])
case class GlobalElapsedTime(time: Int)
case class ProcessElapsedTime(time: Int)
case class CPUSensorValues(
  timeInStates: TimeInStates,
  globalElapsedTime: GlobalElapsedTime,
  processElapsedTime: ProcessElapsedTime,
  tick: Tick)

class CPUSensor extends Actor with Configuration with ActorLogging {

  class Frequency {
    lazy val timeInStateFiles = {
      val timesInState = fromConf("timesInState")(elt => (elt \\ "@value").text)(0)
      val cores = fromConf("cores")(elt => (elt \\ "@value").text.toInt)(0)
      for (core <- 0 until cores) yield (timesInState replace ("%?", core.toString))
    }
    lazy val TimeInStateFormat = """(\d+)\s+(\d+)""".r

    def timeInStates = {
      val result = collection.mutable.HashMap[Int, Int]()

      // TODO inform when time_in_state file is inaccessible 
      for {
        timeInStateFile <- timeInStateFiles
        file = new File(timeInStateFile)
        if (file canRead)
        line <- Source.fromFile(file).getLines
      } {
        line match {
          case TimeInStateFormat(frequency, time) =>
            result += (frequency.toInt -> (time.toInt + (result getOrElse (frequency.toInt, 0))))
          case _ => log.warning("unable to parse line \"" + line + "\" from file \"" + timeInStateFile)
        }
      }

      result.toMap[Int, Int]
    }
  }

  class Time {
    lazy val globalStatFile = fromConf("globalStat")(elt => (elt \\ "@value").text)(0)
    lazy val GlobalStatFormat = """cpu\s+([\d\s]+)""".r
    def globalElapsedTime = {
      val statFile = new File(globalStatFile)
      if (statFile canRead) {
        val line = Source.fromFile(statFile).getLine(0)
        line match {
          case GlobalStatFormat(times) => List.fromString(times, ' ').foldLeft(0) { (acc, x) => (acc + x.toInt) }
          case _ => {
            log.warning("unable to parse line \"" + line + "\" from file \"" + statFile)
            -1
          }
        }
      } else {
        log.warning("unable to read file \"" + statFile + "\"")
        -1
      }
    }

    lazy val processStatFile = fromConf("processStat")(elt => (elt \\ "@value").text)(0)
    def processElapsedTime(implicit process: Process) = {
      val statFile = new File(processStatFile.replace("%?", process.pid.toString))
      if (statFile canRead) {
        val line = Source.fromFile(statFile).getLine(0) split ("""\s""")
        // User time + System time + Block IO waiting time
        line(13).toInt + line(14).toInt + line(41).toInt
      } else {
        log.warning("unable to read file \"" + statFile + "\"")
        -1
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
  lazy val time = new Time

  def receive = {
    case tick: Tick => process(tick)
  }

  def process(tick: Tick) {
    publish(
      CPUSensorValues(
        TimeInStates(timeInStates),
        GlobalElapsedTime(elapsedTime),
        ProcessElapsedTime(elapsedTime(tick.subscription.process)),
        tick))
  }

  def timeInStates = frequency.timeInStates

  def elapsedTime(implicit process: Process = Process(-1)) = time.elapsedTime

  def publish(sensorValues: CPUSensorValues) {
    context.system.eventStream publish sensorValues
  }
}
