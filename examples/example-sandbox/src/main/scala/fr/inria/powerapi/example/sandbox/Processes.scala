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
package fr.inria.powerapi.example.sandbox

import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.processor.aggregator.device.DeviceAggregator
import fr.inria.powerapi.reporter.jfreechart.JFreeChartReporter

/**
 * Set of different use cases of energy monitoring.
 *
 * @author abourdon
 */
object Processes {

  def currentToChart() {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(
      process = Process(currentPid),
      duration = 1 second,
      processor = classOf[DeviceAggregator],
      listener = classOf[JFreeChartReporter]
    )
    Thread.sleep((1 minute).toMillis)
    PowerAPI.stopMonitoring(
      process = Process(currentPid),
      duration = 1 second,
      processor = classOf[DeviceAggregator],
      listener = classOf[JFreeChartReporter]
    )
  }
}