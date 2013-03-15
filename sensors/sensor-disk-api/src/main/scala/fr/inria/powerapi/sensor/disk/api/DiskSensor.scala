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
package fr.inria.powerapi.sensor.disk.api

import fr.inria.powerapi.core.Sensor
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.SensorMessage

/**
 * DiskSensor's results values.
 *
 * @param rw, Ridden and written bytes which the process did cause to be fetched from the storage layer.
 * As a system can have several disks, values are gather into a Map.
 * Each disk is identified by the String key value. Each read and byte counters are identified by the couple (Long, Long).
 * @param tick, the original Tick responsible of the computation of this DiskSensorMessage.
 *
 * @author abourdon
 */
case class DiskSensorMessage(rw: Map[String, (Long, Long)], tick: Tick) extends SensorMessage

/**
 * Base trait for disk sensor modules.
 *
 * @author abourdon
 */
trait DiskSensor extends Sensor