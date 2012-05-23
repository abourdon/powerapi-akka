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
package fr.inria.powerapi.library
import akka.actor.{ Props, ActorSystem, ActorPath }
import akka.dispatch.Await
import akka.util.duration._
import akka.util.{ Timeout, Duration }
import akka.pattern.ask
import fr.inria.powerapi.core.{ Message, MessagesToListen, Listener, Component, TickIt, UnTickIt, TickSubscription, Process }

class Engine {
  val modules = collection.mutable.HashMap[Class[_ <: Component], ActorPath]()
  val listeners = collection.mutable.HashMap[Class[_ <: Listener], ActorPath]()
  implicit val timeout = Timeout(5 seconds)

  def startTick(process: Process, duration: Duration)(implicit system: ActorSystem) {
    system.eventStream.publish(TickIt(TickSubscription(process, duration)))
  }

  def stopTick(process: Process, duration: Duration)(implicit system: ActorSystem) {
    system.eventStream.publish(UnTickIt(TickSubscription(process, duration)))
  }

  def startListener(listenerType: Class[_ <: Listener])(implicit system: ActorSystem) {
    if (!listeners.contains(listenerType)) {
      val listener = system.actorOf(Props(listenerType.newInstance), name = listenerType.getCanonicalName)
      val messages = Await.result(listener ? MessagesToListen, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]
      messages.foreach(message => system.eventStream.subscribe(listener, message))
      listeners += (listenerType -> listener.path)
    }
  }

  def stopListener(listenerType: Class[_ <: Listener])(implicit system: ActorSystem) {
    try {
      val listener = system.actorFor(listeners(listenerType))
      val messages = Await.result(listener ? MessagesToListen, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]
      messages.foreach(message => system.eventStream.unsubscribe(listener, message))
      system stop listener
      listeners -= listenerType
    } catch {
      case nsee: NoSuchElementException => println(nsee.getMessage)
    }
  }

  def startModule(moduleType: Class[_ <: Component])(implicit system: ActorSystem) {
    val module = system.actorOf(Props(moduleType.newInstance), name = moduleType.getCanonicalName)
    val messages = Await.result(module ? MessagesToListen, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]
    messages.foreach(message => system.eventStream.subscribe(module, message))
    modules += (moduleType -> module.path)
  }

  def stopModule(moduleType: Class[_ <: Component])(implicit system: ActorSystem) {
    try {
      val module = system.actorFor(modules(moduleType))
      val messages = Await.result(module ? MessagesToListen, timeout.duration).asInstanceOf[Array[Class[_ <: Message]]]
      messages.foreach(message => system.eventStream.unsubscribe(module, message))
      system stop module
      modules -= moduleType
    } catch {
      case nsee: NoSuchElementException => println(nsee.getMessage)
    }
  }
}

object PowerAPI {
  lazy val engine = new Engine
  implicit val system = ActorSystem("PowerAPI")

  def startModules(moduleTypes: Array[Class[_ <: Component]]) {
    moduleTypes.foreach(moduleType => engine.startModule(moduleType))
  }

  def stopModules(moduleTypes: Array[Class[_ <: Component]]) {
    moduleTypes.foreach(moduleType => engine.stopModule(moduleType))
  }

  def startMonitoring(process: Process, duration: Duration, listenerType: Class[_ <: Listener]) {
    engine.startTick(process, duration)
    engine.startListener(listenerType)
  }

  def stopMonitoring(process: Process, duration: Duration) {
    engine.stopTick(process, duration)
  }

  def stopMonitoring(listenerType: Class[_ <: Listener]) {
    engine.stopListener(listenerType)
  }

  def stopMonitoring(process: Process, duration: Duration, listenerType: Class[_ <: Listener]) {
    stopMonitoring(listenerType)
    stopMonitoring(process, duration)
  }

}