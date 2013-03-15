package fr.inria.powerapi.sensor.powerspy

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.util.duration._
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import org.junit.Ignore

class PowerSpySensorListener extends Listener {
  def messagesToListen = Array(classOf[PowerSpySensorMessage])

  def process(powerSpySensorMessage: PowerSpySensorMessage) {
    println("Received " + powerSpySensorMessage.power)
  }

  def acquire = {
    case powerSpySensorMessage: PowerSpySensorMessage => process(powerSpySensorMessage)
  }
}

class PowerSpySensorSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Ignore
  @Test
  def testTick() {
    PowerAPI.startEnergyModule(classOf[PowerSpySensor])
    PowerAPI.startMonitoring(process = Process(1), duration = 500 milliseconds, listener = classOf[PowerSpySensorListener])

    Thread.sleep((30 seconds).toMillis)

    PowerAPI.stopMonitoring(process = Process(1), duration = 500 milliseconds, listener = classOf[PowerSpySensorListener])
    PowerAPI.stopEnergyModule(classOf[PowerSpySensor])
  }

}