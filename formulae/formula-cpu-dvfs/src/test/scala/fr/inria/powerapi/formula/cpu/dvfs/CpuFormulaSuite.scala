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
package fr.inria.powerapi.formula.cpu.dvfs

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.formula.cpu.api.CpuFormulaMessage
import fr.inria.powerapi.sensor.cpu.api.CpuSensorMessage
import fr.inria.powerapi.sensor.cpu.api.ProcessPercent
import fr.inria.powerapi.sensor.cpu.api.TimeInStates

case object Timestamps

case object NumberOfTicks

class TickReceiver extends Actor with ActorLogging {
  val timestamps = collection.mutable.Set[Long]()
  var numberOfTicks = 0

  def receive = {
    case tick: Tick => {
      numberOfTicks += 1
      timestamps += tick.timestamp
    }
    case Timestamps => sender ! timestamps.toSet
    case NumberOfTicks => sender ! numberOfTicks
  }
}

class CpuFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("cpuformulasuite")
  val cpuformula = TestActorRef[CpuFormula]

  @Test
  def testTdp() {
    cpuformula.underlyingActor.tdp should equal(105)
  }

  @Test
  def testVoltages() {
    cpuformula.underlyingActor.frequencies should equal(Map(
      1800002 -> 1.31,
      2100002 -> 1.41,
      2400003 -> 1.5
    ))
  }

  @Test
  def testConstant() {
    cpuformula.underlyingActor.constant should equal((105 * 0.7) / (2400003 * math.pow(1.5, 2)))
  }

  @Test
  def testPowers() {
    cpuformula.underlyingActor.powers should equal(Map(
      1800002 -> cpuformula.underlyingActor.constant * 1800002 * math.pow(1.31, 2),
      2100002 -> cpuformula.underlyingActor.constant * 2100002 * math.pow(1.41, 2),
      2400003 -> cpuformula.underlyingActor.constant * 2400003 * math.pow(1.5, 2)
    ))
  }

  @Test
  def testPower() {
    val timeInStates = TimeInStates(Map(
      1800002 -> 1,
      2100002 -> 2,
      2400003 -> 3
    ))

    cpuformula.underlyingActor.power(
      CpuSensorMessage(
        timeInStates = timeInStates,
        tick = null
      )
    ) should equal(
        (
          (
            (cpuformula.underlyingActor.powers(1800002) * 1) +
            (cpuformula.underlyingActor.powers(2100002) * 2) +
            (cpuformula.underlyingActor.powers(2400003) * 3)
          ) / (1 + 2 + 3)
        )
      )
  }

  @Test
  def testCompute() {
    val timeInStates = TimeInStates(Map(
      1800002 -> 1,
      2100002 -> 2,
      2400003 -> 3
    ))

    val processPercent = ProcessPercent(0.5)

    cpuformula.underlyingActor.compute(
      CpuSensorMessage(
        timeInStates = timeInStates,
        processPercent = processPercent,
        tick = null
      )
    ) should equal(CpuFormulaMessage(
        energy = Energy.fromPower(
          (
            (
              (cpuformula.underlyingActor.powers(1800002) * 1) +
              (cpuformula.underlyingActor.powers(2100002) * 2) +
              (cpuformula.underlyingActor.powers(2400003) * 3)
            ) / (1 + 2 + 3)
          ) * processPercent.percent
        ),
        device = "cpu",
        tick = null)
      )
  }

}