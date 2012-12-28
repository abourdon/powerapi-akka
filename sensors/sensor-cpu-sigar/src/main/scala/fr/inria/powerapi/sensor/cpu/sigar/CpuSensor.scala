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
package fr.inria.powerapi.sensor.cpu.sigar

import org.hyperic.sigar.Sigar
import org.hyperic.sigar.SigarException
import org.hyperic.sigar.SigarProxyCache

import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.ProcessPercent
import fr.inria.powerapi.sensor.sigar.SigarSensor

/**
 * CPU sensor component using the Hyperic SIGAR API to get hardware information.
 *
 * @see http://www.hyperic.com/products/sigar
 *
 * @author abourdon
 */
class CpuSensor extends fr.inria.powerapi.sensor.cpu.api.CpuSensor with SigarSensor {

  /**
   * SIGAR's proxy instance.
   */
  lazy val sigar = SigarProxyCache.newInstance(new Sigar(), 100)

  /**
   * CPU cores number.
   */
  lazy val cores = sigar.getCpuInfoList()(0).getTotalCores()

  def processPercent(process: Process) = {
    try {
      ProcessPercent(sigar.getProcCpu(process.pid).getPercent() / cores)
    } catch {
      case se: SigarException =>
        log.warning(se.getMessage())
        ProcessPercent(0)
    }
  }

  def process(tick: Tick) {
    publish(
      CpuSensorMessage(
        processPercent = processPercent(tick.subscription.process),
        tick = tick))
  }

}