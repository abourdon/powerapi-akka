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
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.listener.cpu.file
import com.typesafe.config.ConfigException

import scalax.file.Path

trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val filePath =
    try {
      conf.getString("powerapi.listener.cpu-console.file-path")
    } catch {
      case ce: ConfigException => Path.createTempFile(
        prefix = "powerapi.listener-cpu-file",
        deleteOnExit = false).path
    }
  lazy val append =
    try {
      conf.getBoolean("powerapi.listener.cpu-console.append")
    } catch {
      case ce: ConfigException => true
    }
  lazy val justPower = try {
    conf.getBoolean("powerapi.listener.cpu-console.just-power")
  } catch {
    case ce: ConfigException => false
  }

}