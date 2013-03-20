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
package fr.inria.powerapi.listener.aggregator

import fr.inria.powerapi.core.DevicedMessage
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.FormulaMessage
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickedMessage
import fr.inria.powerapi.core.TickSubscription

/**
 * Gather any kind of aggregators.
 *
 * Aggregator can process FormulaMessages in order to make an high level message, thus an "aggregated" message.
 *
 * @author abourdon
 */

/**
 * Messages that can be sent by aggregators:
 * 1. RowMessage, wrapper for a FormulaMessage which is replaced by a ProcessedMessage in order to differ from it (by type)
 * 2. AggregatedMessage, RowMessage and AggregatedMessage composite
 *
 * @author abourdon
 */
@deprecated("Moved to fr.inria.powerapi:core", "1.5")
trait ProcessedMessage extends TickedMessage with DevicedMessage {
  def energy: Energy
}
@deprecated("Moved to processors package", "1.5")
case class RowMessage(energy: Energy, device: String, tick: Tick) extends ProcessedMessage
@deprecated("Moved to processors package", "1.5")
case class AggregatedMessage(tick: Tick, device: String = "timestamp", messages: collection.mutable.Set[ProcessedMessage] = collection.mutable.Set[ProcessedMessage]()) extends ProcessedMessage {
  def energy = Energy.fromPower(messages.foldLeft(0: Double) { (acc, message) => acc + message.energy.power })
  def add(message: ProcessedMessage) {
    messages += message
  }
  def +=(message: ProcessedMessage) {
    add(message)
  }
}

/**
 * Aggregates FormulaMessages by their timestamp.
 *
 * @author abourdon
 */
@deprecated("Moved to processors package", "1.5")
class TimestampAggregator extends Listener {
  // Cache has to be created during the instance creation in order to limit overhead
  // and thus reduce latency when receiving formula messages.
  // That's why we have to define it lazyless
  val cache = collection.mutable.Map[Long, AggregatedMessage]()

  def messagesToListen = Array(classOf[fr.inria.powerapi.core.FormulaMessage])

  def addToCache(implicit formulaMessage: FormulaMessage) {
    cache get formulaMessage.tick.timestamp match {
      case Some(agg) => agg += RowMessage(formulaMessage.energy, formulaMessage.device, formulaMessage.tick)
      case None => {
        val agg = AggregatedMessage(formulaMessage.tick)
        agg += RowMessage(formulaMessage.energy, formulaMessage.device, formulaMessage.tick)
        cache += formulaMessage.tick.timestamp -> agg
      }
    }
  }

  def dropFromCache(implicit timestamp: Long) {
    cache -= timestamp
  }

  def send(implicit timestamp: Long) {
    publish(cache(timestamp))
  }

  def process(implicit formulaMessage: FormulaMessage) {
    if (!cache.isEmpty && !cache.contains(formulaMessage.tick.timestamp)) {
      implicit val toDisplay = cache.minBy(_._1)._1
      send
      dropFromCache
    }
    addToCache
  }

  def acquire = {
    case formulaMessage: FormulaMessage => process(formulaMessage)
  }
}

/**
 * Aggregates FormulaMessages by their timestamp and device.
 *
 * @author abourdon
 */
@deprecated("Moved to processors package", "1.5")
class DeviceAggregator extends TimestampAggregator {
  def byDevices(implicit timestamp: Long): Iterable[AggregatedMessage] = {
    val base = cache(timestamp)
    for (byDevice <- base.messages.groupBy(_.device)) yield (AggregatedMessage(Tick(TickSubscription(Process(-1), base.tick.subscription.duration), timestamp), byDevice._1, byDevice._2))
  }

  override def send(implicit timestamp: Long) {
    byDevices foreach publish
  }
}

/**
 * Aggregates FormulaMessages by their timestamp and process.
 *
 * @author abourdon
 */
@deprecated("Moved to processors package", "1.5")
class ProcessAggregator extends TimestampAggregator {
  def byProcesses(implicit timestamp: Long): Iterable[AggregatedMessage] = {
    val base = cache(timestamp)
    for (byProcess <- base.messages.groupBy(_.tick.subscription.process)) yield (AggregatedMessage(tick = Tick(TickSubscription(byProcess._1, base.tick.subscription.duration), timestamp), messages = byProcess._2))
  }

  override def send(implicit timestamp: Long) {
    byProcesses foreach publish
  }
}