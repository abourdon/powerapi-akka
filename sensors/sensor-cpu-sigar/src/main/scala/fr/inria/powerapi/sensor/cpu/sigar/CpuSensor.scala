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
package fr.inria.powerapi.sensor.cpu.sigar

import scala.collection.JavaConversions

import org.hyperic.sigar.Sigar
import org.hyperic.sigar.SigarException
import org.hyperic.sigar.SigarProxyCache

import com.typesafe.config.ConfigFactory

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.ProcessPercent
import scalax.file.PathMatcher.IsFile
import scalax.file.Path
import scalax.io.Resource

/**
 * Initializer utility object, copying SIGAR dynamic libraries to a readable directory
 * for the java.library.path variable.
 *
 * @see http://www.hyperic.com/products/sigar
 *
 * @author abourdon
 */
trait Initializer {

  /**
   * Do the initialization process:
   * 1. Read the powerapi.sensor-cpu-sigar.sigar-dist property value from the sigar-cpu-sigar property file
   * 2. Iterate over library paths from the powerapi.sensor-cpu-sigar.sigar-dist property value
   * 3. For each path, copy the related library to a readable directory
   * 4. Set the java.library.path variable to this readable directory
   *
   * @return true if success, false otherwise
   */
  def init() = {
    val conf = ConfigFactory.load("sensor-cpu-sigar")
    val dir = Path.createTempDirectory()
    val libs = conf.getStringList("powerapi.sensor-cpu-sigar.sigar-dist")
    JavaConversions.asScalaBuffer(libs).foreach(lib =>
      Resource.fromInputStream(
        getClass().getResourceAsStream(lib)).copyDataTo(
          Path.fromString(dir.path + '/' + lib.substring(lib.lastIndexOf('/')))))
    System.setProperty("java.library.path", dir.path)
    dir.children(IsFile).size.equals(libs.size)
  }

}

/**
 * CPU sensor component using the Hyperic SIGAR API to get hardware information.
 *
 * @see http://www.hyperic.com/products/sigar
 *
 * @author abourdon
 */
class CpuSensor extends fr.inria.powerapi.sensor.cpu.api.CpuSensor with Initializer {

  /**
   * SIGAR's proxy instance.
   */
  lazy val sigar = SigarProxyCache.newInstance(new Sigar(), 100)

  /**
   * CPU cores number.
   */
  lazy val cores = sigar.getCpuInfoList()(0).getTotalCores()

  if (!init) {
    log.warning("unable to initialize the sensor. 'java.library.path' variable may have not been correctly set")
  }

  def processPercent(process: Process) = {
    try {
      ProcessPercent(sigar.getProcCpu(process.pid).getPercent() / cores)
    } catch {
      case se: SigarException =>
        log.warning(se.getMessage())
        ProcessPercent(0)
    }
  }

  def process(tick: Tick) {
    publish(
      CpuSensorMessage(
        processPercent = processPercent(tick.subscription.process),
        tick = tick))
  }

}