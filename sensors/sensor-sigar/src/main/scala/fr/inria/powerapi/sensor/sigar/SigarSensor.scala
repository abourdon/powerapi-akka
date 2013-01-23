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
package fr.inria.powerapi.sensor.sigar
import scala.collection.JavaConversions

import com.typesafe.config.ConfigFactory

import fr.inria.powerapi.core.Sensor
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
trait SigarInitializer {

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
    val conf = ConfigFactory.load("sigar")
    val dir = Path.createTempDirectory(prefix = "powerapi-sensor-sigar")
    val libs = conf.getStringList("sigar.sigar-dist")
    JavaConversions.asScalaBuffer(libs).foreach(lib =>
      Resource.fromInputStream(
        getClass().getResourceAsStream(lib)).copyDataTo(
          Path.fromString(dir.path + '/' + lib.substring(lib.lastIndexOf('/')))))
    System.setProperty("java.library.path", dir.path)
    dir.children(IsFile).size.equals(libs.size)
  }

  if (!init) {
    sys.error("unable to initialize the sensor. 'java.library.path' variable may have not been correctly set")
  }

}

/**
 * Base trait for each Sensor module using the SIGAR API.
 *
 * @see http://www.hyperic.com/products/sigar
 *
 * @author abourdon
 */
trait SigarSensor extends Sensor with SigarInitializer {

}