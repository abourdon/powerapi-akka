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
package fr.inria.powerapi.formula.powerspy

import scala.concurrent.{Lock, Await}
import scala.concurrent.duration.DurationInt


import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.sensor.powerspy.PowerSpySensorMessage
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.sensor.powerspy.PowerSpySensor
import org.junit.Ignore

case object GiveMeYourLastReceive

class PowerSpyFormulaListener extends Listener {
  var powerSpyFormulaMessage: PowerSpyFormulaMessage = null
  val powerSpyFormulaMessageLock = new Lock()

  def messagesToListen = Array(classOf[PowerSpyFormulaMessage])

  def process(powerSpyFormulaMessage: PowerSpyFormulaMessage) {
    if (log.isDebugEnabled) {
      log.debug("Received: " + powerSpyFormulaMessage)
    }
    powerSpyFormulaMessageLock.acquire
    this.powerSpyFormulaMessage = powerSpyFormulaMessage
    powerSpyFormulaMessageLock.release
  }

  def processGiveMeYourLastReceive(sender: ActorRef) {
    powerSpyFormulaMessageLock.acquire
    sender ! powerSpyFormulaMessage
    powerSpyFormulaMessageLock.release
  }

  def acquire = {
    case powerSpyFormulaMessage: PowerSpyFormulaMessage => process(powerSpyFormulaMessage)
    case GiveMeYourLastReceive => processGiveMeYourLastReceive(sender)
  }
}

class PowerSpyFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testComputation() {
    implicit val system = ActorSystem("formula-powerspy")

    val powerSpyFormulaListener = TestActorRef[PowerSpyFormulaListener]
    system.eventStream.subscribe(powerSpyFormulaListener, classOf[PowerSpyFormulaMessage])

    val powerSpyFormula = TestActorRef[PowerSpyFormula]
    powerSpyFormula ! PowerSpySensorMessage(10.0, 0.08f, 0.0005f, null)

    Thread.sleep((1 second).toMillis)

    implicit val timeout = Timeout(5 seconds)
    val result = Await.result(powerSpyFormulaListener ? GiveMeYourLastReceive, timeout.duration).asInstanceOf[PowerSpyFormulaMessage]

    result.energy should equal(Energy.fromPower(10.0 * 0.08f * 0.0005f))
    system.shutdown
  }

  @Ignore
  @Test
  def testAll() {
    Array(classOf[PowerSpySensor], classOf[PowerSpyFormula]).foreach(module => PowerAPI.startEnergyModule(module))

    PowerAPI.startMonitoring(process = Process(1), duration = 500.milliseconds, listener = classOf[PowerSpyFormulaListener])
    Thread.sleep((30.seconds).toMillis)
    PowerAPI.stopMonitoring(process = Process(1), duration = 500.milliseconds, listener = classOf[PowerSpyFormulaListener])

    Array(classOf[PowerSpySensor], classOf[PowerSpyFormula]).foreach(module => PowerAPI.stopEnergyModule(module))
  }

}