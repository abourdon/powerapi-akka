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
package fr.inria.powerapi.sensor.mem.api

import fr.inria.powerapi.core.SensorMessage
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.Sensor

/**
 * Message provided by a memory sensor.
 *
 * @param residentPerc, process resident memory usage percent.
 *
 * @see resident memory
 */
case class MemSensorMessage(residentPerc: Double = 0.0, tick: Tick) extends SensorMessage

/**
 * Base trait for each memory sensor module.
 *
 * @author abourdon
 */
trait MemSensor extends Sensor