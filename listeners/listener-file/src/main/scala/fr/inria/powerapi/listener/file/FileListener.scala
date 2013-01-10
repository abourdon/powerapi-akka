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
package fr.inria.powerapi.listener.file

import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.listener.aggregator.AggregatedMessage
import fr.inria.powerapi.listener.aggregator.DeviceAggregator
import scalax.io.Resource
import scalax.file.Path

/**
 * FileListener's configuration part.
 *
 * @author abourdon
 */
trait Configuration extends fr.inria.powerapi.core.Configuration {
  /**
   * The output file path, build from prefix given by user.
   * Temporary file as default.
   */
  lazy val filePath = load(_.getString("powerapi.listener.file.prefix") + System.nanoTime())(Path.createTempFile(prefix = "powerapi.listener-file", deleteOnExit = false).path)
}

/**
 * Listen to AggregatedMessage and display its content into a given file.
 *
 * @author abourdon
 */
class FileListener extends Listener with Configuration {

  case class Line(aggregatedMessage: AggregatedMessage) {
    override def toString() =
      "timestamp=" + aggregatedMessage.tick.timestamp + ";" +
        "process=" + aggregatedMessage.tick.subscription.process + ";" +
        "device=" + aggregatedMessage.device + ";" +
        "power=" + aggregatedMessage.energy.power + scalax.io.Line.Terminators.NewLine.sep
  }

  lazy val output = {
    log.info("using " + filePath + " as output file")
    Resource.fromFile(filePath)
  }

  def messagesToListen = Array(classOf[AggregatedMessage])

  def process(aggregatedMessage: AggregatedMessage) {
    output.append(Line(aggregatedMessage).toString)
  }

  def acquire = {
    case aggreagatedMessage: AggregatedMessage => process(aggreagatedMessage)
  }
}