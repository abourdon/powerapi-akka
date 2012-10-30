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

import akka.util.duration.intToDurationInt
import akka.util.Duration

/**
 * Energy information wrapper.
 *
 * Note that constructor is private, so it needs to deals with
 * its associated companion object to create new instances.
 *
 * @param power: the power value of the Energy class
 *
 * @author abourdon
 */
case class Energy private (val power: Double) {
  def +(that: Energy) = new Energy(power + that.power)

  def mkString = power.toString

  override def equals(other: Any) = other match {
    case that: Energy => math.abs(power - that.power) <= 0.0001
    case _ => false
  }
}

/**
 * Energy class companion object providing factories to instantiate
 * energy wrapper objects.
 *
 * @author abourdon
 */
object Energy {
  def fromPower(power: Double) = new Energy(power)

  def fromJoule(joule: Double, duration: Duration = 1 second) = new Energy(joule / (duration.toMillis / 1000.0))
}