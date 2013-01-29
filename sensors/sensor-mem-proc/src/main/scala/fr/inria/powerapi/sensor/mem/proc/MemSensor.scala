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
package fr.inria.powerapi.sensor.mem.proc

import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import scalax.io.Line
import scalax.io.Resource
import fr.inria.powerapi.sensor.mem.api.MemSensorMessage

/**
 * Memory sensor specific configuration part.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * Path to the meminfo file, gathering information about system memory.
   *
   * file:///proc/meminfo as default.
   */
  lazy val meminfoPath = load { _.getString("powerapi.mem.global-meminfo") }("file:///proc/meminfo")

  /**
   * Path to the process's status file, gathering information about memory used by a specific process.
   *
   * file:///proc/%?/status as default.
   */
  lazy val statusPath = load { _.getString("powerapi.mem.process-status") }("file:///proc/%?/status")
}

/**
 * Implements the PowerAPI memory Sensor module for systems based on a procfs/sysfs virtual filesystems,
 * typically used by standard Linux distributions.
 *
 * @see http://linux.die.net/man/5/proc.
 */
class MemSensor extends fr.inria.powerapi.sensor.mem.api.MemSensor with Configuration {

  class Resident {
    lazy val MemTotalPattern = """(?s).*MemTotal:\s*(\d+).*""".r
    lazy val memTotal: Long =
      try {
        Resource.fromInputStream(new FileInputStream(new URL(meminfoPath).getPath)).lines(
          Line.Terminators.Auto, true).mkString match {
            case MemTotalPattern(total) => total.toLong
            case _ => throw new IOException("unable to find total memory value from " + meminfoPath + " file")
          }
      } catch {
        case ioe: IOException => {
          if (log.isWarningEnabled) log.warning("i/o exception: " + ioe.getMessage)
          0
        }
      }

    lazy val VmRSSPattern = """(?s).*VmRSS:\s*(\d+).*""".r
    def vmRSS(process: Process): Long =
      try {
        Resource.fromInputStream(new FileInputStream(new URL(statusPath replace ("%?", process.pid.toString)).getPath)).lines(
          Line.Terminators.Auto, true).mkString match {
            case VmRSSPattern(total) => total.toLong
            case _ => throw new IOException("unable to find process resident memory value from " + statusPath + " file for the process " + process.pid)
          }
      } catch {
        case ioe: IOException => {
          if (log.isWarningEnabled) log.warning("i/o exception: " + ioe.getMessage)
          0
        }
      }

    def perc(tick: Tick) = {
      if (memTotal == 0) {
        0
      } else {
        vmRSS(tick.subscription.process).doubleValue / memTotal
      }
    }
  }

  lazy val resident = new Resident

  def process(tick: Tick) {
    publish(MemSensorMessage(resident.perc(tick), tick))
  }

}