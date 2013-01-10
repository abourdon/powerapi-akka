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
package fr.inria.powerapi.example.monitor.linux

import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.listener.aggregator.DeviceAggregator
import fr.inria.powerapi.listener.file.FileListener

/**
 * Set of different use cases of energy monitoring.
 *
 * @author abourdon
 */
object Processes {
  /**
   * Current process monitoring.
   * Values are aggregating by devices.
   */
  def current() {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(listenerType = classOf[fr.inria.powerapi.listener.aggregator.DeviceAggregator])
    PowerAPI.startMonitoring(
      Process(currentPid),
      1 second,
      classOf[fr.inria.powerapi.listener.file.FileListener]
    )
    Thread.sleep((5 minutes).toMillis)
    PowerAPI.stopMonitoring(
      Process(currentPid),
      1 second,
      classOf[fr.inria.powerapi.listener.file.FileListener]
    )
    PowerAPI.stopMonitoring(listenerType = classOf[fr.inria.powerapi.listener.aggregator.DeviceAggregator])
  }
}