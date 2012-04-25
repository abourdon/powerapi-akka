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
import java.io.FileNotFoundException
import java.io.IOException
import java.io.FileInputStream
import java.io.BufferedInputStream

/** Messages definition */
case class TimeInStates(times: Map[Int, Int]) {
  def -(that: TimeInStates) =
    TimeInStates((for ((frequency, time) <- times) yield (frequency, time - that.times.getOrElse(frequency, 0))).toMap)
}
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
      for (timeInStateFile <- timeInStateFiles) {
        try {
          val input = new FileInputStream(timeInStateFile)
          try {
            for (line <- Source.fromInputStream(input).getLines) {
              line match {
                case TimeInStateFormat(frequency, time) => result += (frequency.toInt -> (time.toInt + (result getOrElse (frequency.toInt, 0))))
                case _                                  => log.warning("unable to parse line \"" + line + "\" from file \"" + timeInStateFile)
              }
            }
          } catch {
            case ioe: IOException => log.warning("i/o " + ioe.getMessage)
            case e: Exception     => log.warning("e " + e.getMessage)
          } finally {
            input.close
          }
        } catch {
          case fnfe: FileNotFoundException => log.warning("unable to read file " + timeInStateFile)
        }
      }
      result.toMap[Int, Int]
    }
  }

  class Time {
    lazy val globalStatFile = fromConf("globalStat")(elt => (elt \\ "@value").text)(0)
    lazy val GlobalStatFormat = """cpu\s+([\d\s]+)""".r
    def globalElapsedTime = {
      var result = 0
      try {
        val input = new FileInputStream(globalStatFile)
        try {
          result = {
            Source.fromInputStream(input).getLines.toIndexedSeq(0) match {
              case GlobalStatFormat(times) => times.split(' ').foldLeft(0: Int) { (acc, x) => (acc + x.toInt) }
              case _ => {
                log.warning("unable to parse line from file \"" + globalStatFile)
                0
              }
            }
          }
        } catch {
          case ioe: IOException => log.warning("i/o " + ioe.getMessage)
          case e: Exception     => log.warning("e " + e.getMessage)
        } finally {
          input.close
        }
      } catch {
        case fnfe: FileNotFoundException => log.warning("unable to read global stat file " + globalStatFile);
      }
      result
    }

    lazy val processStatFile = fromConf("processStat")(elt => (elt \\ "@value").text)(0)
    def processElapsedTime(implicit process: Process) = {
      var result = 0
      try {
        val input = new FileInputStream(processStatFile.replace("%?", process.pid.toString))
        try {
          val line = Source.fromInputStream(input).getLines.toIndexedSeq(0) split ("\\s")
          // User time + System time + Block IO waiting time
          result = (line(13).toInt + line(14).toInt + line(41).toInt)
        } catch {

          case ioe: IOException => log.warning("i/o " + ioe.getMessage)
          case e: Exception     => log.warning("e " + e.getMessage)
        } finally {
          input.close
        }
      } catch {
        case fnfe: FileNotFoundException => log.warning("unable to read process stat file for process " + process);
      }
      result
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

  def receive = {
    case tick: Tick => process(tick)
  }

  def publish(sensorValues: CPUSensorValues) {
    context.system.eventStream publish sensorValues
  }

  def process(tick: Tick) {
    publish(
      CPUSensorValues(
        TimeInStates(timeInStates),
        GlobalElapsedTime(elapsedTime),
        ProcessElapsedTime(elapsedTime(tick.subscription.process)),
        tick))
  }

}
