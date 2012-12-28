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
package fr.inria.powerapi.sensor.mem.sigar

import org.hyperic.sigar.Sigar
import org.hyperic.sigar.SigarException
import org.hyperic.sigar.SigarProxyCache

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.sensor.mem.api.MemSensorMessage
import fr.inria.powerapi.sensor.sigar.SigarSensor

/**
 * Memory sensor component using the Hyperic SIGAR API to get hardware information.
 *
 * @see http://www.hyperic.com/products/sigar
 *
 * @author abourdon
 */
class MemSensor extends fr.inria.powerapi.sensor.mem.api.MemSensor with SigarSensor {

  /**
   * SIGAR's proxy instance.
   */
  lazy val sigar = SigarProxyCache.newInstance(new Sigar(), 100)

  def residentPerc(process: Process): Double =
    try {
      sigar.getProcMem(process.pid).getResident().doubleValue / sigar.getMem().getTotal()
    } catch {
      case se: SigarException =>
        log.warning(se.getMessage())
        0
    }

  def process(tick: Tick) {
    publish(MemSensorMessage(residentPerc = residentPerc(tick.subscription.process), tick))
  }

}