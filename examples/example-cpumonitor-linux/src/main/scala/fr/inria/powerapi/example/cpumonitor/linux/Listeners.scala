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
package fr.inria.powerapi.example.cpumonitor.linux

import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.listener.cpu.jfreechart.Chart
import fr.inria.powerapi.listener.cpu.jfreechart.CpuListener

/**
 * CPU Listener which filter received information before display it into a graph.
 *
 * @author abourdon
 */
class FilteredChart extends CpuListener {
  val powers = collection.mutable.HashMap[Process, Double]()

  override def acquire = {
    case cpuFormulaMessage: CpuFormulaMessage => {
      val process = cpuFormulaMessage.tick.subscription.process
      val old = powers getOrElse (process, Double.PositiveInfinity)
      val now = cpuFormulaMessage.energy.power
      // We only process the chart if the difference between old and new value are over 10%
      if (math.min(now, old) / math.max(now, old) < 0.1) {
        powers += (process -> now)
        Chart.process(cpuFormulaMessage)
      }
    }
  }
}

/**
 * CPU listener which gather all CpuFormulaMessage in order to compute only one result
 * as the sum of all received CpuFormulaMessage for a specific timestamp.
 *
 * @author abourdon
 */
class GatheredChart extends CpuListener {
  val cache = collection.mutable.HashMap[Long, Double]()

  override def process(cpuFormulaMessage: CpuFormulaMessage) {
    def gatherPowers(cpuFormulaMessage: CpuFormulaMessage) {
      cache(cpuFormulaMessage.tick.timestamp) += cpuFormulaMessage.energy.power
    }

    def displayCache(cpuFormulaMessage: CpuFormulaMessage) {
      if (!cache.isEmpty) {
        val timestamp = cache.keySet.toIndexedSeq(0)
        Chart.process(
          CpuFormulaMessage(
            Energy.fromPower(cache(timestamp)),
            Tick(TickSubscription(Process(-1), cpuFormulaMessage.tick.subscription.duration))))
      }
    }

    def updateTimestamp(cpuFormulaMessage: CpuFormulaMessage) {
      if (!cache.isEmpty) {
        val oldTimestamp = cache.keySet.toIndexedSeq(0)
        cache -= oldTimestamp
      }
      cache += (cpuFormulaMessage.tick.timestamp -> 0)
    }

    if (cache.contains(cpuFormulaMessage.tick.timestamp)) {
      gatherPowers(cpuFormulaMessage)
    } else {
      displayCache(cpuFormulaMessage)
      updateTimestamp(cpuFormulaMessage)
    }
  }
}