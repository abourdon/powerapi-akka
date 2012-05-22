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
package powerapi.sensor.cpusensor.linux

import com.typesafe.config.Config

trait Configuration extends powerapi.core.Configuration {
  lazy val cores = conf.getInt("powerapi.cpu.cores")
  lazy val globalStatPath = conf.getString("powerapi.cpu.global-stat")
  lazy val processStatPath = conf.getString("powerapi.cpu.process-stat")
  lazy val timeInStatePath = conf.getString("powerapi.cpu.time-in-state")
}