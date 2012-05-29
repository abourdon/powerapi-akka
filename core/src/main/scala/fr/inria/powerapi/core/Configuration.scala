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
package fr.inria.powerapi.core
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException
import com.typesafe.config.Config

/**
 * Base trait that deals with configuration files using the Typesafe Config library.
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
   * @param request: closure symbolizing request to get information from configuration file.
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