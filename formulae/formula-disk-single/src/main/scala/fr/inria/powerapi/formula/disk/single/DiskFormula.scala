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
package fr.inria.powerapi.formula.disk.single
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import fr.inria.powerapi.sensor.disk.api.DiskSensorValues

trait Configuration extends fr.inria.powerapi.core.Configuration {
  implicit def toRate(str: String) = new Rate(str)

  class Rate(str: String) {
    lazy val RateFormat = """([\d\,.]+)([mMgG])([bB])/s""".r

    def fromRateToDouble = str match {
      case RateFormat(number, multiplier, unit) => try {
        number.replace(',', '.').toDouble * (multiplier match {
          case "m" | "M" => 1000000
          case "g" | "G" => 1000000000
        }) * (unit match {
          case "b" => 1.0 / 8
          case "B" => 1
        })
      } catch {
        case nfe: NumberFormatException => {
          log.warning("number format exception: " + nfe.getMessage)
          0: Double
        }
      }
      case _ =>
        log.warning("unable to parse " + str + " as a Rate format")
        0: Double
    }
  }

  lazy val readPower = load(_.getDouble("powerapi.disk.read-power"))(0)
  lazy val writePower = load(_.getDouble("powerapi.disk.write-power"))(0)
  lazy val maxRate = load(_.getString("powerapi.disk.max-rate").fromRateToDouble)(0)
}

class DiskFormula extends fr.inria.powerapi.formula.disk.api.DiskFormula with Configuration {
  lazy val readPowerByByte = readPower / maxRate
  lazy val writePowerByByte = writePower / maxRate

  lazy val cache = collection.mutable.Map[TickSubscription, DiskSensorValues]()
  lazy val defaultSensorValue = DiskSensorValues(Map("n/a" -> (0: Long, 0: Long)), null)

  def power(now: DiskSensorValues, old: DiskSensorValues) = try {
    (now.rw("n/a")._1 - old.rw("n/a")._1) * readPowerByByte +
      (now.rw("n/a")._2 - old.rw("n/a")._2) * writePowerByByte
  } catch {
    case nsee: NoSuchElementException => {
      log.warning("no such element exception: " + nsee.getMessage)
      0: Double
    }
  }

  def compute(now: DiskSensorValues): DiskFormulaValues = {
    val old = cache getOrElse (now.tick.subscription, defaultSensorValue)
    DiskFormulaValues(Energy.fromPower(power(now, old)), now.tick)
  }

  def refreshCache(now: DiskSensorValues) {
    cache += (now.tick.subscription -> now)
  }

  def process(diskSensorValues: DiskSensorValues) {
    publish(compute(diskSensorValues))
    refreshCache(diskSensorValues)
  }
}