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
    lazy val RateFormat = """([\d\,.]+)([MG])B/s""".r
    def fromRateToDouble = str match {
      case RateFormat(number, multiplier) => try {
        number.replace(',', '.').toDouble * (multiplier match {
          //          case "M" => 1048576.0 // 1 << 20, 2^20
          //          case "G" => 1073741824.0 // 1 << 30, 2^30
          case "M" => 1000000.0 // As we talk in bytes, we can simply add the 10^6 multiplier
          case "G" => 1000000000.0 // As we talk in bytes, we can simply add the 10^9 multiplier
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
  lazy val readRate = load(_.getString("powerapi.disk.read-rate").fromRateToDouble)(0)
  lazy val writePower = load(_.getDouble("powerapi.disk.write-power"))(0)
  lazy val writeRate = load(_.getString("powerapi.disk.write-rate").fromRateToDouble)(0)
}

class DiskFormula extends fr.inria.powerapi.formula.disk.api.DiskFormula with Configuration {
  lazy val readEnergyByByte = readPower / readRate
  lazy val writeEnergyByByte = writePower / writeRate

  lazy val cache = collection.mutable.Map[TickSubscription, DiskSensorValues]()
  lazy val defaultSensorValue = DiskSensorValues(Map("n/a" -> (0: Long, 0: Long)), null)

  def power(now: DiskSensorValues, old: DiskSensorValues) = try {
    val duration = now.tick.subscription.duration.toMillis / 1000.0
    Energy.fromJoule(((now.rw("n/a")._1 - old.rw("n/a")._1) * readEnergyByByte + (now.rw("n/a")._2 - old.rw("n/a")._2) * writeEnergyByByte), now.tick.subscription.duration)
  } catch {
    case nsee: NoSuchElementException => {
      log.warning("no such element exception: " + nsee.getMessage)
      Energy.fromPower(0)
    }
  }

  def compute(now: DiskSensorValues): DiskFormulaValues = {
    val old = cache getOrElse (now.tick.subscription, now)
    DiskFormulaValues(power(now, old), now.tick)
  }

  def refreshCache(now: DiskSensorValues) {
    cache += (now.tick.subscription -> now)
  }

  def process(diskSensorValues: DiskSensorValues) {
    publish(compute(diskSensorValues))
    refreshCache(diskSensorValues)
  }
}