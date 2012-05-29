/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.core
import scala.collection.mutable.HashMap
import akka.util.Duration
import scala.collection.mutable.SynchronizedMap
import akka.actor.Cancellable
import akka.util.duration.intToDurationInt
import com.typesafe.config.ConfigException

/**
 * Clock's messages definition.
 * 
 * @author abourdon
 */
case class TickSubscription(process: Process, duration: Duration)
case class TickIt(subscription: TickSubscription) extends Message
case class UnTickIt(subscription: TickSubscription) extends Message
case class Tick(subscription: TickSubscription, timestamp: Long = System.currentTimeMillis) extends Message

/**
 * Clock configuration.
 *
 * @author abourdon
 */
trait ClockConfiguration extends Configuration {
  lazy val minimumTickDuration = load { conf => Duration.parse(conf.getString("akka.scheduler.tick-duration")) }(10 milliseconds)
}

/**
 * Clock component, that "tick" the event bus following a configured period.
 *
 * The PowerAPI architecture is based on a asynchronous architecture composed by several components.
 * Each component listen to an event bus and reacts following messages emit by the event bus.
 * Thus, each component is in a passive state and only process its business part following the emit message.
 *
 * At the bottom of this architecture, the Clock component provide a "tick" message to wake up
 * components which are listen to it.
 *
 * Clock component reacts to both TickIt and UnTickIt messages which respectively ask to
 * start/stop a periodically sending of a Tick message.
 *
 * @author abourdon
 */
class Clock extends Component with ClockConfiguration {
  val subscriptions = new HashMap[Duration, Set[TickSubscription]] with SynchronizedMap[Duration, Set[TickSubscription]]
  val schedulers = new HashMap[Duration, Cancellable]
  val system = context.system

  def makeItTick(implicit tickIt: TickIt) {
    def subscribe(implicit tickIt: TickIt) {
      val currentSubscriptions = subscriptions getOrElse (tickIt.subscription.duration, Set[TickSubscription]())
      subscriptions += (tickIt.subscription.duration -> (currentSubscriptions + tickIt.subscription))
    }

    def scheduleRegistration(implicit tickIt: TickIt) {
      val duration = if (tickIt.subscription.duration < minimumTickDuration) {
        log.warning("unable to schedule a duration less than that specified in the configuration file (" + tickIt.subscription.duration + " vs " + minimumTickDuration)
        minimumTickDuration
      } else {
        tickIt.subscription.duration
      }
      if (!(schedulers contains duration)) {
        schedulers += (duration -> system.scheduler.schedule(Duration.Zero, duration)({
          if (subscriptions contains duration) {
            val timestamp = System.currentTimeMillis
            subscriptions(duration).foreach(subscription => publish(Tick(subscription, timestamp)))
          }
        }))
      }
    }

    subscribe
    scheduleRegistration
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

  def process = {
    case subscribe: TickIt => makeItTick(subscribe)
    case unsubscribe: UnTickIt => unmakeItTick(unsubscribe)
    case unknown => throw new UnsupportedOperationException("unable to process message " + unknown)
  }
}