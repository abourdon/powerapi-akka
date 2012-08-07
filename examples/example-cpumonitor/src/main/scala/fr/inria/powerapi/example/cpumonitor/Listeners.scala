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
package fr.inria.powerapi.example.cpumonitor

import fr.inria.powerapi.core.{Tick, Energy, Process, TickSubscription}
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.listener.cpu.jfreechart.{CpuListener, Chart}

/**
 * CPU Listener which filter received information before display it into a graph.
 *
 * @author abourdon
 */
class FilteredChart extends CpuListener {
  val powers = collection.mutable.HashMap[Process, Double]()

  override def acquire = {
    case cpuFormulaValues: CpuFormulaValues => {
      val process = cpuFormulaValues.tick.subscription.process
      val old = powers getOrElse(process, Double.PositiveInfinity)
      val now = cpuFormulaValues.energy.power
      // We only process the chart if the difference between old and new value are over 10%
      if (math.min(now, old) / math.max(now, old) < 0.1) {
        powers += (process -> now)
        Chart.process(cpuFormulaValues)
      }
    }
  }
}

/**
 * CPU listener which gather all CpuFormulaValues in order to compute only one result
 * as the sum of all received CpuFormulaValues for a specific timestamp.
 *
 * @author abourdon
 */
class GatheredChart extends CpuListener {
  val cache = collection.mutable.HashMap[Long, Double]()

  override def process(cpuFormulaValues: CpuFormulaValues) {
    def gatherPowers(cpuFormulaValues: CpuFormulaValues) {
      cache(cpuFormulaValues.tick.timestamp) += cpuFormulaValues.energy.power
    }

    def displayCache(cpuFormulaValues: CpuFormulaValues) {
      if (!cache.isEmpty) {
        val timestamp = cache.keySet.toIndexedSeq(0)
        Chart.process(
          CpuFormulaValues(
            Energy.fromPower(cache(timestamp)),
            Tick(TickSubscription(Process(-1), cpuFormulaValues.tick.subscription.duration))))
      }
    }

    def updateTimestamp(cpuFormulaValues: CpuFormulaValues) {
      if (!cache.isEmpty) {
        val oldTimestamp = cache.keySet.toIndexedSeq(0)
        cache -= oldTimestamp
      }
      cache += (cpuFormulaValues.tick.timestamp -> 0)
    }

    if (cache.contains(cpuFormulaValues.tick.timestamp)) {
      gatherPowers(cpuFormulaValues)
    } else {
      displayCache(cpuFormulaValues)
      updateTimestamp(cpuFormulaValues)
    }
  }
}