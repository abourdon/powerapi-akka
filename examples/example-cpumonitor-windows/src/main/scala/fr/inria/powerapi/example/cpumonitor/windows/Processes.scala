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

import java.util.Timer
import java.util.TimerTask

import scala.collection.JavaConversions

import com.typesafe.config.ConfigFactory

import akka.util.duration.intToDurationInt
import akka.util.Duration
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.listener.cpu.file.CpuListener
import fr.inria.powerapi.listener.cpu.jfreechart.CpuListener
import scalax.io.Resource

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
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    ))
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(
      Process(pid),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    ))
  }

  /**
   * CPU monitoring which hardly specifying the monitored process.
   */
  def perso() {
    PowerAPI.startMonitoring(
      Process(12758),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(
      Process(12758),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
  }

  /**
   * CPU monitoring wich hardly specifying the monitored process and write results into a file.
   */
  def persoFile() {
    pids.foreach(pid => PowerAPI.startMonitoring(
      Process(pid),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.file.CpuListener]
    ))
    Thread.sleep((5 minutes).toMillis)
    pids.foreach(pid => PowerAPI.stopMonitoring(
      Process(pid),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.file.CpuListener]
    ))
  }

  /**
   * Current process CPU monitoring.
   */
  def current() {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(
      Process(currentPid),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(
      Process(currentPid),
      1 second,
      classOf[fr.inria.powerapi.listener.cpu.jfreechart.CpuListener]
    )
  }

  /**
   * Intensive process CPU monitoring in periodically scanning all current processes.
   */
  def intensive() {
    def getPids = {
      val PSFormat = """^PID:\s*(\d+).*""".r
      val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array(
        "TASKLIST",
        //"/FI", "\"USERNAME ne NT AUTHORITY\\SYSTEM\"",
        //"/FI", "\"STATUS eq running\"",
        "/FO", "\"LIST\"")
      ).getInputStream).lines().toList.map({
        pid =>
          pid match {
            case PSFormat(id) => id.toInt
            case other => -1
          }
      })
      pids.drop(-1)
      pids
    }

    val pids = scala.collection.mutable.Set[Int]()
    val dur = 1 second
    def udpateMonitoredPids() {
      val currentPids = scala.collection.mutable.Set[Int](getPids: _*)

      val oldPids = pids -- currentPids
      oldPids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = dur))
      pids --= oldPids

      val newPids = currentPids -- pids
      newPids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = dur))
      pids ++= newPids
    }

    PowerAPI.startMonitoring(listenerType = classOf[GatheredChart])
    val timer = new Timer
    timer.scheduleAtFixedRate(new TimerTask() {
      def run() {
        udpateMonitoredPids
      }
    }, Duration.Zero.toMillis, (250 milliseconds).toMillis)

    Thread.sleep((1 hour).toMillis)
    timer.cancel
    PowerAPI.stopMonitoring(listenerType = classOf[GatheredChart])
  }

}

