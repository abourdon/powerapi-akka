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
package powerapi.formula.cpuformula
import java.lang.management.ManagementFactory
import java.net.URL

import scala.collection.mutable.HashMap
import scala.io.Source
import scala.util.Random
import scala.xml.XML

import org.junit.Ignore
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
import powerapi.core.Energy
import powerapi.core.Process
import powerapi.core.TickIt
import powerapi.core.TickSubscription
import powerapi.core.UnTickIt
import powerapi.sensor.cpusensor.CPUSensorValues
import powerapi.sensor.cpusensor.CPUSensor
import powerapi.sensor.cpusensor.GlobalElapsedTime
import powerapi.sensor.cpusensor.ProcessElapsedTime
import powerapi.sensor.cpusensor.TimeInStates
import scalax.file.Path
import scalax.io.Resource

class SimpleTickReceiver extends Actor with ActorLogging {
  var receivedTicks = 0

  def receive = {
    case tick: Tick => receivedTicks += 1
  }
}

class CPUFormulaLoggingReceiver extends Actor with ActorLogging {
  def receive = {
    case cpuFormulaValues: CPUFormulaValues =>
      if (cpuFormulaValues.energy.power > 0) {
        log.info(cpuFormulaValues.tick.subscription.process + ": " + cpuFormulaValues.energy
        )
      }
  }
}

class CPUFormulaWritingReceiver extends Actor with ActorLogging {
  lazy val out = Path.createTempFile(
    prefix = "powerapi",
    deleteOnExit = false
  )

  def receive = {
    case cpuFormulaValues: CPUFormulaValues => {
      out append (cpuFormulaValues.energy.power.toString + "\n")
    }
  }
}

case object ProcessAggregation
class CPUFormulaAggregatingReceiver extends Actor with ActorLogging {
  lazy val out = Path.createTempFile(
    prefix = "powerapi-aggregate",
    deleteOnExit = false
  )
  lazy val map = HashMap[Long, List[Energy]]()
  lazy val set = scala.collection.mutable.Set[Long]()

  def receive = {
    case cpuFormulaValues: CPUFormulaValues => {
      map += (cpuFormulaValues.tick.timestamp -> ((map getOrElse (cpuFormulaValues.tick.timestamp, List[Energy]())) ::: List(cpuFormulaValues.energy)))
      set += cpuFormulaValues.tick.timestamp
    }
    case ProcessAggregation =>
      map foreach ({ item => out append ((item._2.foldLeft(Energy.fromPower(0)) { (acc, x) => acc + x }).mkString + "\n") })
  }
}

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
    case Timestamps    => sender ! timestamps.toSet
    case NumberOfTicks => sender ! numberOfTicks
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
    cpuformula.voltages should have size (3)
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
    cpuformula.powers should have size (3)
    cpuformula.powers.foreach(power => power._2 should equal(
      cpuformula.constant * power._1 * math.pow(cpuformula.voltages(power._1), 2)
    ))
  }

  @Test
  def testRefreshCache {
    val old = CPUSensorValues(
      TimeInStates(Map[Int, Int]()),
      GlobalElapsedTime(100),
      ProcessElapsedTime(50),
      Tick(TickSubscription(Process(123), 500 milliseconds))
    )
    cpuformula.refreshCache(old)
    cpuformula.cache getOrElse (500 milliseconds, null) should equal(old)

    val now = CPUSensorValues(
      TimeInStates(Map[Int, Int]()),
      GlobalElapsedTime(300),
      ProcessElapsedTime(80),
      Tick(TickSubscription(Process(123), 500 milliseconds))
    )
    cpuformula.refreshCache(now)
    cpuformula.cache getOrElse (500 milliseconds, null) should equal(now)

    cpuformula.cache getOrElse (123 milliseconds, null) should be(null)
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

    cpuformula.power(old, now) should equal(totalPowers / totalTimes / cpuformula.cores)
  }

  trait ConfigurationMock extends Configuration {
    lazy val timeInState = new URL("file", "/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state", "").toString
    lazy val frequencies = {
      val lines = Resource.fromURL(timeInState.replace("%?", "1")).lines().toList
      (for (line <- lines) yield (line.split("\\s")(0), Random.nextDouble.toString)).map(item => <frequency value={ item._1 } voltage={ item._2 }/>)
    }

    override lazy val conf =
      <powerapi>
        <tdp value="105"/>
        <cores value="4"/>
        <globalStat url="file:///proc/stat"/>
        <processStat url="file:///proc/%?/stat"/>
        <timesInState url={ timeInState }/>
        <frequencies>
          { frequencies }
        </frequencies>
      </powerapi>
  }

  @Test
  def testCurrentProcess {
    val clock = system.actorOf(Props[Clock])
    val cpuformulaReceiver = system.actorOf(Props[CPUFormulaWritingReceiver], name = "cpuformulareceiver")
    val cpusensor = system.actorOf(Props(new CPUSensor with ConfigurationMock), name = "cpusensor")
    val cpuFormula = system.actorOf(Props(new CPUFormula with ConfigurationMock), name = "cpuformula")

    system.eventStream subscribe (cpusensor, classOf[Tick])
    system.eventStream subscribe (cpuFormula, classOf[CPUSensorValues])
    system.eventStream subscribe (cpuformulaReceiver, classOf[CPUFormulaValues])

    clock ! TickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 1 second))
    Thread.sleep(2000)
    clock ! TickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 500 milliseconds))
    Thread.sleep(5000)
    clock ! UnTickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 500 milliseconds))
    Thread.sleep(2000)
    clock ! UnTickIt(TickSubscription(Process(ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt), 1 second))
  }

  @Ignore
  @Test
  def testGivenProcess {
    val clock = system.actorOf(Props[Clock])
    val cpuformulaReceiver = system.actorOf(Props[CPUFormulaLoggingReceiver], name = "cpuformulareceiver")
    val cpusensor = system.actorOf(Props(new CPUSensor with Configuration {
      override lazy val conf = XML.load(getClass.getResourceAsStream("/powerapi-dellprecision.xml"))
    }), name = "cpusensor")
    val cpuFormula = system.actorOf(Props(new CPUFormula with Configuration {
      override lazy val conf = XML.load(getClass.getResourceAsStream("/powerapi-dellprecision.xml"))
    }), name = "cpuformula")

    system.eventStream subscribe (cpusensor, classOf[Tick])
    system.eventStream subscribe (cpuFormula, classOf[CPUSensorValues])
    system.eventStream subscribe (cpuformulaReceiver, classOf[CPUFormulaValues])

    clock ! TickIt(TickSubscription(Process(1234), 500 milliseconds))
    Thread.sleep(300000)
    clock ! UnTickIt(TickSubscription(Process(1234), 500 milliseconds))
  }

  @Test
  def testIntensive {
    val clock = system.actorOf(Props[Clock])
    val cpusensor = system.actorOf(Props(new CPUSensor with ConfigurationMock), name = "cpusensor")
    val cpuFormula = system.actorOf(Props(new CPUFormula with ConfigurationMock), name = "cpuformula")
    val cpuformulaReceiver = TestActorRef[CPUFormulaAggregatingReceiver]
    val simpleTickReceiver = TestActorRef[SimpleTickReceiver]

    system.eventStream subscribe (cpusensor, classOf[Tick])
    system.eventStream subscribe (cpuFormula, classOf[CPUSensorValues])
    system.eventStream subscribe (cpuformulaReceiver, classOf[CPUFormulaValues])
    system.eventStream subscribe (simpleTickReceiver, classOf[Tick])

    val PSFormat = """^\s*(\d+).*""".r
    val pids = Source.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).getLines.toList.map({ pid =>
      pid match {
        case PSFormat(pid) => pid.toInt
        case _             => 1
      }
    })
    val duration = 100 milliseconds
    val sleep = 5000
    pids.foreach(pid => clock ! TickIt(TickSubscription(Process(pid), duration)))
    Thread.sleep(5000)
    pids.foreach(pid => clock ! UnTickIt(TickSubscription(Process(pid), duration)))

    println(
      simpleTickReceiver.underlyingActor.receivedTicks + " ticks received, for " + pids.size + " pid analyzed." +
        "(Theorical / Real number of timestamp processed) = (" + (simpleTickReceiver.underlyingActor.receivedTicks / pids.size) + ", " + cpuformulaReceiver.underlyingActor.map.size + ")"
    )
  }

}