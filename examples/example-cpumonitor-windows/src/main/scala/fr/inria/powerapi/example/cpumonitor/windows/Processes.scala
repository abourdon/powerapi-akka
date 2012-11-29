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
package fr.inria.powerapi.example.cpumonitor.windows

import scala.collection.JavaConversions

import com.typesafe.config.ConfigFactory

import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.listener.cpu.file.CpuListener
import fr.inria.powerapi.listener.cpu.jfreechart.CpuListener

/**
 * Set of different use cases of CPU energy monitoring.
 *
 * @author abourdon
 */
object Processes {
  lazy val conf = ConfigFactory.load
  lazy val pids = JavaConversions.asScalaBuffer(conf.getIntList("powerapi.pids")).toList

  /**
   * Process CPU monitoring using information given by the configuration file.
   */
  def fromConf() {
    pids.foreach(pid => PowerAPI.startMonitoring(
      Process(pid),
      500 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener])
    )
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(
      Process(pid),
      500 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener])
    )
  }

  /**
   * CPU monitoring which hardly specifying the monitored process.
   */
  def perso() {
    PowerAPI.startMonitoring(
      Process(16463),
      2000 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(
      Process(16463),
      2000 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
  }

  /**
   * CPU monitoring wich hardly specifying the monitored process and write results into a file.
   */
  def persoFile() {
    pids.foreach(pid => PowerAPI.startMonitoring(
      Process(pid),
      500 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.file.CpuListener])
    )
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(
      Process(pid),
      500 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.file.CpuListener])
    )
  }

  /**
   * Current process CPU monitoring.
   */
  def current() {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(
      Process(currentPid),
      500 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(
      Process(currentPid),
      500 milliseconds,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
  }

}

