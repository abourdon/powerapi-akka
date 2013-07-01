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

import akka.actor.Cancellable
import scala.concurrent.duration.{FiniteDuration, Duration, DurationInt}
import collection.mutable
import java.util.concurrent.TimeUnit

/**
 * Clock's messages definition.
 *
 * @author abourdon
 */
case class TickIt(subscription: TickSubscription) extends Message
case class UnTickIt(subscription: TickSubscription) extends Message

/**
 * Clock configuration.
 *
 * @author abourdon
 */
trait ClockConfiguration extends Configuration {
  lazy val minimumTickDuration = load { conf =>
    Duration.create(conf.getString("akka.scheduler.tick-duration")) match {
      case Duration(length, unit) => FiniteDuration(length, unit)
    }
  }(10.milliseconds)
}

/**
 * Clock component, that "tick" the event bus following a configured period.
 *
 * The PowerAPI architecture is based on a asynchronous architecture composed by several components.
 * Each component listen to an event bus and reacts following messages sent by the event bus.
 * Thus, each component is in a passive state and only run its business part following the sent message.
 *
 * At the bottom of this architecture, the Clock component provides a "tick" message to wake up
 * components which are listen to it.
 *
 * Clock component reacts to both TickIt and UnTickIt messages which respectively ask to
 * start/stop a periodically sending of a Tick message.
 *
 * @author abourdon
 */
class Clock extends Component with ClockConfiguration {
  val subscriptions = new mutable.HashMap[FiniteDuration, Set[TickSubscription]] with mutable.SynchronizedMap[FiniteDuration, Set[TickSubscription]]
  val schedulers = new mutable.HashMap[FiniteDuration, Cancellable]

  def makeItTick(implicit tickIt: TickIt) {
    def subscribe(implicit tickIt: TickIt) {
      val currentSubscriptions = subscriptions getOrElse (tickIt.subscription.duration, Set[TickSubscription]())
      subscriptions += (tickIt.subscription.duration -> (currentSubscriptions + tickIt.subscription))
    }

    def schedule(implicit tickIt: TickIt) {
      val duration = if (tickIt.subscription.duration < minimumTickDuration) {
        if (log.isWarningEnabled) log.warning("unable to schedule a duration less than that specified in the configuration file (" + tickIt.subscription.duration + " vs " + minimumTickDuration)
        minimumTickDuration
      } else {
        tickIt.subscription.duration
      }
      if (!(schedulers contains duration)) {
        schedulers += (duration -> context.system.scheduler.schedule(Duration.Zero, duration)({
          if (subscriptions contains duration) {
            val timestamp = System.currentTimeMillis
            subscriptions(duration).foreach(subscription => publish(Tick(subscription, timestamp)))
          }
        })(context.system.dispatcher))
      }
    }

    subscribe
    schedule
  }

  def unmakeItTick(implicit untickIt: UnTickIt) {
    def unsubscribe(implicit untickIt: UnTickIt) {
      val currentSubscriptions = subscriptions getOrElse (untickIt.subscription.duration, Set[TickSubscription]())
      if (!currentSubscriptions.isEmpty) {
        subscriptions += (untickIt.subscription.duration -> (currentSubscriptions - untickIt.subscription))
      }
    }

    def unschedule(implicit untickIt: UnTickIt) {
      val duration = untickIt.subscription.duration
      val currentSubscriptions = subscriptions getOrElse (untickIt.subscription.duration, Set[TickSubscription]())

      // Iff subscriptions associated to the specified duration is empty,
      // then we have to stop schedule and delete duration reference from maps.
      if (currentSubscriptions.isEmpty) {
        // Stop schedule associated to the associated duration.
        val schedule = schedulers getOrElse (duration, new Cancellable {
          def cancel() {}

          def isCancelled = true
        })
        schedule.cancel()

        // Delete duration key from subscriptions and schedulers maps.
        subscriptions -= duration
        schedulers -= duration
      }
    }

    unsubscribe
    unschedule
  }

  def messagesToListen = Array(classOf[TickIt], classOf[UnTickIt])

  def acquire = {
    case subscribe: TickIt => makeItTick(subscribe)
    case unsubscribe: UnTickIt => unmakeItTick(unsubscribe)
    case unknown => throw new UnsupportedOperationException("unable to process message " + unknown)
  }
}