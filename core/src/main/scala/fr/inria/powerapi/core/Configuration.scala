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
package fr.inria.powerapi.core
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException
import com.typesafe.config.Config

/**
 * Base trait dealing with configuration files using the Typesafe Config library.
 *
 * @see https://github.com/typesafehub/config
 *
 * @author abourdon
 */
trait Configuration extends Component {
  /**
   * Link to get information from configuration files.
   */
  private lazy val conf = ConfigFactory.load

  /**
   * Default pattern to get information from configuration file.
   *
   * @param request: request to get information from configuration file.
   * @param default: default value returned in case of ConfigException.
   *
   * @see http://typesafehub.github.com/config/latest/api/com/typesafe/config/ConfigException.html
   */
  def load[T](request: Config => T)(default: T): T =
    try {
      request(conf)
    } catch {
      case ce: ConfigException => {
        log.warning(ce.getMessage + " (using " + default + " as default value)")
        default
      }
    }
}