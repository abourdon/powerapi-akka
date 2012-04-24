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
package powerapi.sensor.cpusensor
import java.lang.management.ManagementFactory

import scala.io.Source
import scala.util.Random

import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit

import akka.actor.actorRef2Scala
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import powerapi.core.Tick
import powerapi.core.Clock
import powerapi.core.Configuration
import powerapi.core.Process
import powerapi.core.TickIt
import powerapi.core.TickSubscription
import powerapi.core.UnTickIt
import powerapi.formula.cpuformula.CPUFormulaValues
import powerapi.formula.cpuformula.CPUFormula

class CPUFormulaReceiver extends Actor with ActorLogging {
  def receive = {
    case cpuFormulaValues: CPUFormulaValues => log.debug(cpuFormulaValues.energy.toString)
  }
}

class CPUFormulaSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("cpuformulasuite")
  val cpuformula = TestActorRef[CPUFormula].underlyingActor

  @Test
  def testTdp {
    cpuformula.tdp should equal(105)
  }

  @Test
  def testCores {
    cpuformula.cores should equal(4)
  }

  @Test
  def testVoltages {
    cpuformula.voltages.size should equal(3)
    cpuformula.voltages(1800002) should equal(1.31)
    cpuformula.voltages(2100002) should equal(1.41)
    cpuformula.voltages(2400003) should equal(1.5)
  }

  @Test
  def testConstant {
    cpuformula.constant should equal((0.7 * cpuformula.tdp) / (cpuformula.voltages.max._1 * math.pow(cpuformula.voltages.max._2, 2)))
  }

  @Test
  def testPowers {
    cpuformula.powers.size should equal(3)
    cpuformula.powers.foreach(power => power._2 should equal(
      cpuformula.constant * power._1 * math.pow(cpuformula.voltages(power._1), 2)
    ))
  }

  @Test
  def testUsage {
    val old = CPUSensorValues(
      TimeInStates(Map[Int, Int]()),
      GlobalElapsedTime(100),
      ProcessElapsedTime(50),
      null
    )

    val now = CPUSensorValues(
      TimeInStates(Map[Int, Int]()),
      GlobalElapsedTime(300),
      ProcessElapsedTime(80),
      null
    )

    cpuformula.usage(old, now) should equal((80.0 - 50) / ((300.0 - 100) / cpuformula.cores))
  }

  @Test
  def testPower {
    val oldTimeInStates = TimeInStates(Map[Int, Int](1800002 -> 10, 2100002 -> 20, 2400003 -> 30))
    val old = CPUSensorValues(
      oldTimeInStates,
      GlobalElapsedTime(100),
      ProcessElapsedTime(50),
      null
    )

    val nowTimInStates = TimeInStates(Map[Int, Int](1800002 -> 100, 2100002 -> 200, 2400003 -> 300))
    val now = CPUSensorValues(
      nowTimInStates,
      GlobalElapsedTime(300),
      ProcessElapsedTime(80),
      null
    )

    val diffTimeInStates = nowTimInStates - oldTimeInStates
    val totalPowers = diffTimeInStates.times.foldLeft(0: Double) { (acc, time) => acc + (cpuformula.powers(time._1) * time._2) }
    val totalTimes = diffTimeInStates.times.foldLeft(0) { (acc, time) => acc + time._2 }

    cpuformula.power(old, now) should equal(totalPowers / totalTimes)
  }

  trait ConfigurationMock extends Configuration {
    val timeInState = "/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state"
    val frequencies = {
      val lines = Source.fromFile(timeInState.replace("%?", "1")).getLines
      (for (line <- lines) yield (line.split("\\s")(0), Random.nextDouble.toString)).map(item => <frequency value={ item._1 } voltage={ item._2 }/>)
    }

    override lazy val conf =
      <powerapi>
        <tdp value="105"/>
        <cores value="4"/>
        <globalStat value="/proc/stat/"/>
        <processStat value="/proc/%?/stat"/>
        <timesInState value={ timeInState }/>
        <frequencies>
          { frequencies }
        </frequencies>
      </powerapi>
  }

  @Test
  def testAll {
    val clock = system.actorOf(Props[Clock])
    val cpuformulaReceiver = system.actorOf(Props[CPUFormulaReceiver])
    val cpusensor = system.actorOf(Props(new CPUSensor with ConfigurationMock))
    val cpuFormula = system.actorOf(Props(new CPUFormula with ConfigurationMock))

    system.eventStream subscribe (cpusensor, classOf[Tick])
    system.eventStream subscribe (cpuFormula, classOf[CPUSensorValues])
    system.eventStream subscribe (cpuformulaReceiver, classOf[CPUFormulaValues])

    clock ! TickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 1 seconds))
    Thread.sleep(2000)
    clock ! TickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 500 milliseconds))
    Thread.sleep(5000)
    clock ! UnTickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 500 milliseconds))
    Thread.sleep(2000)
    clock ! UnTickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 1 seconds))
  }

}