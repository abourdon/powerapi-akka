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
package fr.inria.powerapi.sensor.disk.proc
import fr.inria.powerapi.sensor.disk.api.DiskSensorValues
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.Process
import scalax.io.Resource
import java.io.FileInputStream
import java.net.URL
import java.io.IOException

trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val iofile = load(_.getString("powerapi.disk.io"))("file:///proc/%?/io")
}

class DiskSensor extends fr.inria.powerapi.sensor.disk.api.DiskSensor with Configuration {
  /**
   * Get the number of ridden and written bytes of the specified process, since its begining
   */
  def readAndwrite(implicit process: Process): (Long, Long) =
    try {
      // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
      // Then, we simply read these files thanks to a FileInputStream in getting those local path
      val lines = Resource.fromInputStream(new FileInputStream(new URL(iofile replace ("%?", process.pid.toString)).getPath)).lines().toIndexedSeq
      (read(lines(4)), scala.math.max(0, write(lines(5)) - cancelledWrite(lines(6))))
    } catch {
      case ioe: IOException => {
        log.warning("i/o exception: " + ioe.getMessage)
        (0: Long, 0: Long)
      }
    }

  lazy val ReadFormat = """read_bytes:\s+(\d+)""".r
  /**
   * Get the number of ridden bytes from the line given in argument
   */
  def read(line: String) = line match {
    case ReadFormat(readBytes) => readBytes.toLong
    case _ => 0: Long
  }

  lazy val WriteFormat = """write_bytes:\s+(\d+)""".r
  /**
   * Get the number of written bytes from the line given in argument
   */
  def write(line: String) = line match {
    case WriteFormat(writeBytes) => writeBytes.toLong
    case _ => 0: Long
  }

  lazy val CancelledWriteFormat = """cancelled_write_bytes:\s+(\d+)""".r
  /**
   * Get the number of cancelled written bytes from the line given in argument
   */
  def cancelledWrite(line: String) = line match {
    case CancelledWriteFormat(cancelledWriteBytes) => cancelledWriteBytes.toLong
    case _ => 0: Long
  }

  def process(tick: Tick) {
    publish(DiskSensorValues(Map("n/a" -> readAndwrite(tick.subscription.process)), tick))
  }
}