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
package powerapi.cpusensor
import akka.actor.Actor
import powerapi.core.Configuration
import powerapi.core.Tick

case object Tdp
case object NumberOfCores
case object Frequencies
case class Frequency(value: Int, voltage: Double)

class CPUSensor extends Actor with Configuration {
  // Environment specific values (from the configuration file)
  lazy val tdp = fromConf[Double]("tdp") { node => (node \\ "@value").text.toDouble }(0)
  lazy val numberOfCores = fromConf[Int]("numberOfCores") { node => (node \\ "@value").text.toInt }(0)
  lazy val frequencies = fromConf[Frequency]("frequency") { node => Frequency((node \\ "@value").text.toInt, (node \\ "@voltage").text.toDouble) }

  def receive = {
    case Tdp => tdp
    case NumberOfCores => numberOfCores
    case Frequencies => frequencies
    case tick: Tick => process(tick)
  }

  /**
   * Process according to the given tick
   */
  def process(tick: Tick) {
  }

}
