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
package fr.inria.powerapi.library
import akka.actor.{ Props, ActorSystem, ActorPath }
import akka.dispatch.Await
import akka.util.duration._
import akka.util.{ Timeout, Duration }
import akka.pattern.ask
import fr.inria.powerapi.core.{ Message, MessagesToListen, Listener, Component, TickIt, UnTickIt, TickSubscription, Process }
import fr.inria.powerapi.core.Clock
import fr.inria.powerapi.core.EnergyModule
import scala.collection.mutable.SynchronizedMap

/**
 * PowerAPI's messages definition
 *
 * @author abourdon
 */
case class StartComponent(componentType: Class[_ <: Component]) extends Message
case class StopComponent(componentType: Class[_ <: Component]) extends Message
case class StartMonitoring(
  process: Process = Process(-1),
  duration: Duration = Duration.Zero,
  listenerType: Class[_ <: Listener] = null) extends Message
case class StopMonitoring(
  process: Process = Process(-1),
  duration: Duration = Duration.Zero,
  listenerType: Class[_ <: Listener] = null) extends Message

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
  implicit val timeout = Timeout(5 seconds)

  def messagesToListen = Array(classOf[StartComponent], classOf[StopComponent], classOf[StartMonitoring], classOf[StopMonitoring])

  def process = {
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
        log.warning("component " + componentType.getCanonicalName + " already started")
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
      log.debug("Clock started")
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
        log.warning("Component " + componentType.getCanonicalName + " is not started")
      }
    }

    stop(stopComponent.componentType)

    // Be aware to stop the Clock if all other components has been stopped.
    if (components.size == 1 && components.contains(classOf[Clock])) {
      stop(classOf[Clock])
      log.debug("Clock stopped")
    }
  }

  def process(startMonitoring: StartMonitoring) {
    /**
     * Starts the monitoring of a process during a certain duration a listened by a given listener.
     *
     * @param proc: process to monitor.
     * @param duration: monitoring duration period.
     * @param listenerType: type of listener that wants to be aware by monitoring.
     */
    def start(proc: Process, duration: Duration, listenerType: Class[_ <: Listener]) {
      if (listenerType != null) {
        process(StartComponent(listenerType))
      }
      if (proc != Process(-1) && duration != Duration.Zero) {
        context.system.eventStream.publish(TickIt(TickSubscription(proc, duration)))
      }
    }

    start(startMonitoring.process, startMonitoring.duration, startMonitoring.listenerType)
  }

  def process(stopMonitoring: StopMonitoring) {
    /**
     * Stops the monitoring of a process during a certain duration and listened by a given listener.
     *
     * @param proc: process to monitor.
     * @param duration: monitoring duration period.
     * @param listenerType: type of listener that wants to be unaware by monitoring.
     */
    def stop(proc: Process, duration: Duration, listenerType: Class[_ <: Listener]) {
      if (listenerType != null) {
        process(StopComponent(listenerType))
      }
      if (proc != Process(-1) && duration != Duration.Zero) {
        context.system.eventStream.publish(UnTickIt(TickSubscription(proc, duration)))
      }
    }

    stop(stopMonitoring.process, stopMonitoring.duration, stopMonitoring.listenerType)
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
   * Results are listened by the given listener.
   *
   * @param process: process to monitor.
   * @param duration: duration period monitoring.
   * @param listener: listener that wants to listen monitoring results.
   */
  def startMonitoring(process: Process = Process(-1), duration: Duration = Duration.Zero, listenerType: Class[_ <: Listener] = null) {
    engine ! StartMonitoring(process, duration, listenerType)
  }

  /**
   * Stops the monitoring of the given process during the given duration period.
   * An associated listener can also be stopped.
   *
   * @param process: process to stop to monitor.
   * @param duration: duration period monitoring.
   * @param listener: listener that wants to be unaware by monitoring results.
   */
  def stopMonitoring(process: Process = Process(-1), duration: Duration = Duration.Zero, listenerType: Class[_ <: Listener] = null) {
    engine ! StopMonitoring(process, duration, listenerType)
  }
}