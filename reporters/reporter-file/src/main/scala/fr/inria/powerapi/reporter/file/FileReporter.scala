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
package fr.inria.powerapi.reporter.file

import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.library.PowerAPI
import scalax.io.Resource
import scalax.file.Path
import fr.inria.powerapi.core.ProcessedMessage
import fr.inria.powerapi.core.Reporter

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
class FileReporter extends Reporter with Configuration {

  case class Line(processedMessage: ProcessedMessage) {
    override def toString() =
      "timestamp=" + processedMessage.tick.timestamp + ";" +
        "process=" + processedMessage.tick.subscription.process + ";" +
        "device=" + processedMessage.device + ";" +
        "power=" + processedMessage.energy.power + scalax.io.Line.Terminators.NewLine.sep
  }

  lazy val output = {
    if (log.isInfoEnabled) log.info("using " + filePath + " as output file")
    Resource.fromFile(filePath)
  }

  def process(processedMessage: ProcessedMessage) {
    output.append(Line(processedMessage).toString)
  }

}