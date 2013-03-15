package fr.inria.powerapi.sensor.powerspy

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.io.Reader
import java.io.Writer

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.Props
import fr.inria.powerapi.core.Sensor
import fr.inria.powerapi.core.SensorMessage
import fr.inria.powerapi.core.Tick
import javax.microedition.io.Connector
import javax.microedition.io.StreamConnection

case object StartMonitoring
case object StopMonitoring
case object Close
case class PowerSpySensorMessage(power: Double, tick: Tick) extends SensorMessage

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

  override def firePowerUpdated(power: java.lang.Double) {
    if (power != null) {
      context.system.eventStream.publish(PowerSpySensorMessage(power, null))
    }
  }

  def receive() = {
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

  def process(tick: Tick) {
    powerSpyDelegate ! StartMonitoring
  }

  override def postStop() {
    powerSpyDelegate ! StopMonitoring
    powerSpyDelegate ! Close
    super.postStop()
  }

}