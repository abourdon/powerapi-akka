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
package fr.inria.powerapi.library

import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, Duration, DurationInt}

import akka.actor.{ Props, ActorSystem, ActorPath }
import akka.pattern.ask
import akka.util.Timeout
import fr.inria.powerapi.core.Clock
import fr.inria.powerapi.core.EnergyModule
import fr.inria.powerapi.core.{ Message, MessagesToListen, Listener, Component, TickIt, UnTickIt, TickSubscription, Process }
import fr.inria.powerapi.core.Processor
import fr.inria.powerapi.core.Reporter

/**
 * PowerAPI's messages definition
 *
 * @author abourdon
 */
case class StartComponent(componentType: Class[_ <: Component]) extends Message
case class StopComponent(componentType: Class[_ <: Component]) extends Message
case class StartMonitoring(
  process: Process = Process(-1),
  duration: FiniteDuration = Duration.Zero,
  processor: Class[_ <: Processor] = null,
  listener: Class[_ <: Listener] = null) extends Message
case class StopMonitoring(
  process: Process = Process(-1),
  duration: FiniteDuration = Duration.Zero,
  processor: Class[_ <: Processor] = null,
  listener: Class[_ <: Listener] = null) extends Message

/**
 * PowerAPI engine which start/stop every PowerAPI components such as Listener or Energy Module.
 *
 * Current implementation start/stop component in giving the component type.
 * This restriction helps to instantiate only one implementation of a given component.
 *
 * @author abourdon
 */
class PowerAPI extends Component {
  val components = collection.mutable.HashMap[Class[_ <: Component], ActorPath]()
  implicit val timeout = Timeout(5.seconds)

  def messagesToListen = Array(classOf[StartComponent], classOf[StopComponent], classOf[StartMonitoring], classOf[StopMonitoring])

  def acquire = {
    case startComponent: StartComponent => process(startComponent)
    case stopComponent: StopComponent => process(stopComponent)
    case startMonitoring: StartMonitoring => process(startMonitoring)
    case stopMonitoring: StopMonitoring => process(stopMonitoring)
  }

  def process(startComponent: StartComponent) {
    /**
     * Starts a component to work into the PowerAPI system.
     *
     * @param componentType: component type that have to be started.
     */
    def start(componentType: Class[_ <: Component]) {
      if (components.contains(componentType)) {
        if (log.isWarningEnabled) log.warning("component " + componentType.getCanonicalName + " already started")
      } else {
        val component = context.actorOf(Props(componentType.newInstance), name = componentType.getCanonicalName)
        val messages = Await.result(component ? MessagesToListen, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]
        messages.foreach(message => context.system.eventStream.subscribe(component, message))
        components += (componentType -> component.path)
      }
    }

    start(startComponent.componentType)

    // Be aware to start the Clock if a component has been started
    if (components.size > 0 && !components.contains(classOf[Clock])) {
      start(classOf[Clock])
      if (log.isDebugEnabled) log.debug("Clock started")
    }
  }

  def process(stopComponent: StopComponent) {
    /**
     * Stops a component from the PowerAPI system.
     *
     * @param componentType: component type that have to be stopped.
     */
    def stop(componentType: Class[_ <: Component]) {
      if (components.contains(componentType)) {
        val component = context.actorFor(components(componentType))
        val messages = Await.result(component ? MessagesToListen, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]
        messages.foreach(message => context.system.eventStream.unsubscribe(component, message))
        context.stop(component)
        components -= componentType
      } else {
        if (log.isWarningEnabled) log.warning("Component " + componentType.getCanonicalName + " is not started")
      }
    }

    stop(stopComponent.componentType)

    // Be aware to stop the Clock if all other components has been stopped.
    if (components.size == 1 && components.contains(classOf[Clock])) {
      stop(classOf[Clock])
      if (log.isDebugEnabled) log.debug("Clock stopped")
    }
  }

  def process(startMonitoring: StartMonitoring) {
    /**
     * Starts the monitoring of a process during a certain duration a listened by a given listener.
     *
     * @param process: process to monitor.
     * @param duration: duration period monitoring.
     * @param processor: processor type which will be aware by monitoring results.
     * @param listener: listener type which will be aware by processor messages and display final results.
     */
    def start(proc: Process, duration: FiniteDuration, processor: Class[_ <: Processor], listener: Class[_ <: Listener]) {
      if (processor != null) {
        process(StartComponent(processor))
      }
      if (listener != null) {
        process(StartComponent(listener))
      }
      if (proc != Process(-1) && duration != Duration.Zero) {
        context.system.eventStream.publish(TickIt(TickSubscription(proc, duration)))
      }
    }

    start(startMonitoring.process, startMonitoring.duration, startMonitoring.processor, startMonitoring.listener)
  }

  def process(stopMonitoring: StopMonitoring) {
    /**
     * Stops the monitoring of a process during a certain duration and listened by a given listener.
     *
     * @param process: process to stop to monitor.
     * @param duration: duration period monitoring.
     * @param processor : processor type which will be unaware by monitoring results.
     * @param listener: listener type which will be unaware by processor messages.
     */
    def stop(proc: Process, duration: FiniteDuration, processor: Class[_ <: Processor], listener: Class[_ <: Listener]) {
      if (processor != null) {
        process(StopComponent(processor))
      }
      if (listener != null) {
        process(StopComponent(listener))
      }
      if (proc != Process(-1) && duration != Duration.Zero) {
        context.system.eventStream.publish(UnTickIt(TickSubscription(proc, duration)))
      }
    }

    stop(stopMonitoring.process, stopMonitoring.duration, stopMonitoring.processor, stopMonitoring.listener)
  }
}

/**
 * PowerAPI companion object that provide an API to use PowerAPI library.
 *
 * @author abourdon
 */
object PowerAPI {
  implicit lazy val system = ActorSystem("PowerAPI")
  lazy val engine = system.actorOf(Props[PowerAPI])

  /**
   * Starts the energy module associated to the given type.
   *
   * @param energyModuleType: energy module type to start.
   */
  def startEnergyModule(energyModuleType: Class[_ <: EnergyModule]) {
    engine ! StartComponent(energyModuleType)
  }

  /**
   * Stops the energy module associated to the given type.
   *
   * @param energyModuleType: energy module type to stop.
   */
  def stopEnergyModule(energyModuleType: Class[_ <: EnergyModule]) {
    engine ! StopComponent(energyModuleType)
  }

  /**
   * Starts the monitoring of the given process during the given duration period.
   * Results are then processed by the given processor and displayed by the given listener.
   *
   * @param process: process to monitor.
   * @param duration: duration period monitoring.
   * @param processor: processor type which will be aware by monitoring results.
   * @param reporter: reporter type which will be aware by processor messages and display final results.
   */
  def startMonitoring(process: Process = Process(-1), duration: FiniteDuration = Duration.Zero, processor: Class[_ <: Processor] = null, listener: Class[_ <: Listener] = null) {
    engine ! StartMonitoring(process, duration, processor, listener)
  }

  /**
   * Stops the monitoring of the given process during the given duration period.
   * Processors and listeners can also be stopped.
   *
   * @param process: process to stop to monitor.
   * @param duration: duration period monitoring.
   * @param processor : processor type which will be unaware by monitoring results.
   * @param reporter: reporter type which will be unaware by processor messages.
   */
  def stopMonitoring(process: Process = Process(-1), duration: FiniteDuration = Duration.Zero, processor: Class[_ <: Processor] = null, listener: Class[_ <: Listener] = null) {
    engine ! StopMonitoring(process, duration, processor, listener)
  }
}