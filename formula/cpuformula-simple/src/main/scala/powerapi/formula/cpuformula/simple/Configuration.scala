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
package powerapi.formula.cpuformula.simple

import scala.collection.JavaConversions

import com.typesafe.config.Config

trait Configuration extends powerapi.core.Configuration {
  class ExtraConfiguration(conf: Config) {
    def getCores() = conf.getDouble("powerapi.cpu.cores")
    def getTdp() = conf.getDouble("powerapi.cpu.tdp")
    def getFrequencies() =
      (for (item <- JavaConversions.asScalaBuffer(conf.getConfigList("powerapi.cpu.frequencies")))
        yield (item.asInstanceOf[Config].getInt("value"), item.asInstanceOf[Config].getDouble("voltage"))).toMap
  }

  implicit def toExtraConfiguration(conf: Config) = new ExtraConfiguration(conf)
}