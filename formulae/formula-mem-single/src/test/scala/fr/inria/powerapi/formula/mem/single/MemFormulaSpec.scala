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
package fr.inria.powerapi.formula.mem.single
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.FlatSpec
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.core.Process
import akka.util.duration.intToDurationInt
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.formula.mem.api.MemFormulaMessage
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.sensor.mem.api.MemSensorMessage
import fr.inria.powerapi.sensor.mem.sigar.MemSensor

class MemFormulaListener extends Listener {
  def messagesToListen = Array(classOf[MemFormulaMessage])
  def acquire = {
    case memFormulaMessage: MemFormulaMessage => println(memFormulaMessage.energy.power)
    case unknown => log.warning("unknown message " + unknown)
  }
}

@RunWith(classOf[JUnitRunner])
class MemFormulaSpec extends FlatSpec with ShouldMatchersForJUnit {

  trait ConfigurationMock extends Configuration {
    override lazy val readPower = 5.0
    override lazy val writePower = 15.0
  }

  implicit val system = ActorSystem("mem-formula-single")
  val memFormula = TestActorRef(new MemFormula with ConfigurationMock)

  "A MemFormula" should "compute global memory power consumption" in {
    memFormula.underlyingActor.power should equal ((5 + 15).doubleValue / 2)
  }

  "A MemFormula" should "compute process memory power consumption" in {
    memFormula.underlyingActor.compute(MemSensorMessage(residentPerc = 0.5, tick = null)) should equal (memFormula.underlyingActor.power * 0.5)
  }

  "A MemFormula" should "react to Tick to compute process memory power consumption" in {
    val currentPid = java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt

    Array(classOf[MemSensor], classOf[MemFormula]).foreach(PowerAPI.startEnergyModule(_))
    PowerAPI.startMonitoring(process = Process(currentPid), duration = 1 second, listener = classOf[MemFormulaListener])

    Thread.sleep((5 seconds).toMillis)

    PowerAPI.stopMonitoring(process = Process(currentPid), duration = 1 second, listener = classOf[MemFormulaListener])
    Array(classOf[MemSensor], classOf[MemFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

}