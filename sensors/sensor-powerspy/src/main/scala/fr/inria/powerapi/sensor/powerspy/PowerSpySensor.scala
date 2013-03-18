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
package fr.inria.powerapi.sensor.powerspy

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.Reader
import java.io.Writer

import scala.Array.canBuildFrom
import scala.concurrent.Lock

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.Props
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Sensor
import fr.inria.powerapi.core.SensorMessage
import fr.inria.powerapi.core.Tick
import javax.microedition.io.Connector
import javax.microedition.io.StreamConnection

case object StartMonitoring
case object StopMonitoring
case object Close
case class PowerSpySensorMessage(currentRMS: Double, uScale: Float, iScale: Float, tick: Tick) extends SensorMessage
case class PowerSpySensorDelegateMessage(currentRMS: Double, uScale: Float, iScale: Float) extends Message

object PowerSpyDelegate {
  def apply(sppUrl: String): Option[PowerSpyDelegate] = {
    try {
      val connection = Connector.open(sppUrl).asInstanceOf[StreamConnection]
      Some(new PowerSpyDelegate(connection, new BufferedReader(new InputStreamReader(connection.openInputStream())), new PrintWriter(connection.openOutputStream())))
    } catch {
      case e: Exception => None
    }
  }
}

class PowerSpyDelegate(connection: StreamConnection, in: Reader, out: Writer) extends SimplePowerSpy(connection) with Actor {
  setInput(in)
  setOutput(out)

  override def fireDataUpdated(data: PowerSpyEvent) {
    if (data != null) {
      context.system.eventStream.publish(PowerSpySensorDelegateMessage(data.getCurrentRMS(), data.getUScale(), data.getIScale()))
    }
  }

  def receive = {
    case StartMonitoring => startPowerMonitoring()
    case StopMonitoring => stopPowerMonitoring()
    case Close => close()
  }

}

trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val sppUrl = load { _.getString("powerapi.sensor.powerspy.spp-url") }("btspp://nothing")
}

class PowerSpySensor extends Sensor with Configuration {

  lazy val powerSpyDelegate = context.actorOf(Props(PowerSpyDelegate(sppUrl).getOrElse(null)))

  var currentTick: Tick = null
  val currentTickLock = new Lock()

  def process(tick: Tick) {
    currentTickLock.acquire
    currentTick = tick
    currentTickLock.release

    powerSpyDelegate ! StartMonitoring
  }

  def process(powerSpySensorDelegateMessage: PowerSpySensorDelegateMessage) {
    currentTickLock.acquire
    val usableTick = currentTick
    currentTickLock.release

    publish(PowerSpySensorMessage(powerSpySensorDelegateMessage.currentRMS, powerSpySensorDelegateMessage.uScale, powerSpySensorDelegateMessage.iScale, usableTick))
  }

  def acquireDelegate: Receive = {
    case powerSpySensorDelegateMessage: PowerSpySensorDelegateMessage => process(powerSpySensorDelegateMessage)
  }

  override def messagesToListen = super.messagesToListen ++ Array(classOf[PowerSpySensorDelegateMessage])

  override def postStop() {
    powerSpyDelegate ! StopMonitoring
    powerSpyDelegate ! Close
    super.postStop()
  }

  override def acquire: Receive = super.acquire orElse acquireDelegate

}