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
 */
package powerapi.core
import scala.collection.mutable.SynchronizedMap
import scala.collection.mutable.HashMap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Cancellable
import akka.util.duration.intToDurationInt
import akka.util.Duration

/** Messages definition */
case class TickSubscription(process: Process, duration: Duration)
case class TickIt(subscription: TickSubscription)
case class UnTickIt(subscription: TickSubscription)
case class Tick(subscription: TickSubscription, timestamp: Long = System.currentTimeMillis)

class Clock extends Actor with ActorLogging {
  val subscriptions = new HashMap[Duration, Set[TickSubscription]] with SynchronizedMap[Duration, Set[TickSubscription]]
  val schedulers = new HashMap[Duration, Cancellable]
  val system = context.system

  def subscribe(implicit tickIt: TickIt) {
    val currentSubscriptions = subscriptions getOrElse (tickIt.subscription.duration, Set[TickSubscription]())
    subscriptions += (tickIt.subscription.duration -> (currentSubscriptions + tickIt.subscription))
  }

  private def scheduleRegistration(implicit tickIt: TickIt) {
    val duration = tickIt.subscription.duration
    if (!(schedulers contains duration)) {
      schedulers += (duration -> system.scheduler.schedule(0 second, duration)(schedule(duration)))
    }
  }

  private def schedule(duration: Duration) {
    if (subscriptions contains duration) {
      val timestamp = System.currentTimeMillis
      subscriptions(duration).foreach(subscription => (system.eventStream publish Tick(subscription, timestamp)))
    }
  }

  def makeItTick(implicit tickIt: TickIt) {
    subscribe
    scheduleRegistration
  }

  private def unsubscribe(implicit untickIt: UnTickIt) {
    val currentSubscriptions = subscriptions getOrElse (untickIt.subscription.duration, Set[TickSubscription]())
    if (!currentSubscriptions.isEmpty) {
      subscriptions += (untickIt.subscription.duration -> (currentSubscriptions - untickIt.subscription))
    }
  }

  private def unschedule(implicit untickIt: UnTickIt) {
    val duration = untickIt.subscription.duration
    val currentSubscriptions = subscriptions getOrElse (untickIt.subscription.duration, Set[TickSubscription]())

    // Iff subscriptions associated to the specified duration is empty,
    // then we have to stop schedule and delete duration reference from maps
    if (currentSubscriptions.isEmpty) {
      // Stop schedule associated to the associated duration
      val schedule = schedulers getOrElse (duration, new Cancellable {
        def cancel() {}
        def isCancelled = true
      })
      schedule.cancel()

      // Delete duration key from subscriptions and schedulers maps
      subscriptions -= duration
      schedulers -= duration
    }
  }

  def unmakeItTick(implicit untickIt: UnTickIt) {
    unsubscribe
    unschedule
  }

  def receive = {
    case subscribe: TickIt => makeItTick(subscribe)
    case unsubscribe: UnTickIt => unmakeItTick(unsubscribe)
    case unknown => throw new UnsupportedOperationException("unable to process message " + unknown)
  }
}