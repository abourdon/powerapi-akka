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
package fr.inria.powerapi.sensor.disk.atop
import java.io.FileInputStream
import java.io.IOException
import java.net.URL

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.sensor.disk.api.DiskSensorMessage
import scalax.io.Resource

/**
 * Disk sensor configuration part.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * Specific process stat file, typically located to the /proc/[pid]/stat path.
   */
  lazy val processStatPath = load { _.getString("powerapi.disk.process-stat") }("file:///proc/%?/stat")
}

/**
 * Disk sensor collecting data from ATOP linux based kernels.
 *
 * @see http://www.atoptool.nl
 *
 * @author abourdon
 */
class DiskSensor extends fr.inria.powerapi.sensor.disk.api.DiskSensor with Configuration {
  def readAndWrite(implicit process: Process) =
    try {
      // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
      // Then, we simply read these files thanks to a FileInputStream in getting those local path
      val line = Resource.fromInputStream(new FileInputStream(new URL(processStatPath replace ("%?", process.pid.toString)).getPath)).lines().toIndexedSeq(1).toString.split("\\s")
      // Read bytes, Write bytes
      (line(0).toLong, line(2).toLong)
    } catch {
      case ioe: IOException => {
        log.warning("i/o exception: " + ioe.getMessage)
        (0: Long, 0: Long)
      }
    }

  def process(tick: Tick) {
    publish(DiskSensorMessage(Map("n/a" -> readAndWrite(tick.subscription.process)), tick))
  }
}