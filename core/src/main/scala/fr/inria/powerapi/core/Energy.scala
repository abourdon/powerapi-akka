/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.core

import akka.util.Duration
import akka.util.duration.intToDurationInt

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
case class Energy private (power: Double) {
  def +(that: Energy) = new Energy(power + that.power)

  def mkString = power.toString()
}

/**
 * Energy class companion object providing factories to instantiate
 * energy wrapper objects.
 *
 * @author abourdon
 */
object Energy {
  def fromPower(power: Double) = new Energy(power)

  def fromJoule(joule: Double, duration: Duration = 1 second) = new Energy(joule / duration.toSeconds)
}