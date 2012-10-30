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
package fr.inria.powerapi.listener.cpudisk.jfreechart
import java.lang.management.ManagementFactory
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Message
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.formula.disk.api.DiskFormulaValues
import scalax.io.Resource
import akka.util.duration._
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import fr.inria.powerapi.core.Energy
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import akka.util.Duration

trait ConfigurationMock extends Configuration {
  override lazy val refreshRate = Duration.Inf
}

class CpuDiskListenerMock extends CpuDiskListener with ConfigurationMock

class CpuDiskListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testAggregate() {
    implicit val system = ActorSystem("CpuDiskListenerSuite")
    val cpuDiskListener = TestActorRef[CpuDiskListenerMock].underlyingActor

    val timestamp = 0L
    cpuDiskListener.cache should have size 0

    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), timestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(10), Tick(TickSubscription(Process(123), 1 second), timestamp)))
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(2), Tick(TickSubscription(Process(456), 1 second), timestamp)))
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(3), Tick(TickSubscription(Process(789), 1 second), timestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(20), Tick(TickSubscription(Process(456), 1 second), timestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(30), Tick(TickSubscription(Process(789), 1 second), timestamp)))

    cpuDiskListener.cache should equal(Map(
      timestamp -> Map(
        Process(123) -> Map[String, Double]("cpu" -> 1, "disk" -> 10),
        Process(456) -> Map[String, Double]("cpu" -> 2, "disk" -> 20),
        Process(789) -> Map[String, Double]("cpu" -> 3, "disk" -> 30))))

    cpuDiskListener.aggregate(timestamp) should equal(Map[String, Double]("cpu" -> (1 + 2 + 3), "disk" -> (10 + 20 + 30)))
  }

  @Test
  def testClean() {
    implicit val system = ActorSystem("CpuDiskListenerSuite")
    val cpuDiskListener = TestActorRef[CpuDiskListenerMock].underlyingActor

    val timestamp = 0L
    cpuDiskListener.cache should have size 0

    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), timestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(10), Tick(TickSubscription(Process(123), 1 second), timestamp)))
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(2), Tick(TickSubscription(Process(456), 1 second), timestamp)))
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(3), Tick(TickSubscription(Process(789), 1 second), timestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(20), Tick(TickSubscription(Process(456), 1 second), timestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(30), Tick(TickSubscription(Process(789), 1 second), timestamp)))

    val anotherTimestamp = 1L
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), anotherTimestamp)))
    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(10), Tick(TickSubscription(Process(123), 1 second), anotherTimestamp)))
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(2), Tick(TickSubscription(Process(456), 1 second), anotherTimestamp)))

    cpuDiskListener.cache should equal(Map(
      timestamp -> Map(
        Process(123) -> Map[String, Double]("cpu" -> 1, "disk" -> 10),
        Process(456) -> Map[String, Double]("cpu" -> 2, "disk" -> 20),
        Process(789) -> Map[String, Double]("cpu" -> 3, "disk" -> 30)),
      anotherTimestamp -> Map(
        Process(123) -> Map[String, Double]("cpu" -> 1, "disk" -> 10),
        Process(456) -> Map[String, Double]("cpu" -> 2))))

    cpuDiskListener.clean(timestamp)

    cpuDiskListener.cache should equal(Map(
      anotherTimestamp -> Map(
        Process(123) -> Map[String, Double]("cpu" -> 1, "disk" -> 10),
        Process(456) -> Map[String, Double]("cpu" -> 2))))
  }

  @Ignore
  @Test
  def testCacheWhenCacheIsCleanedByTwoMin() {
    implicit val system = ActorSystem("CpuDiskListenerSuite")
    val cpuDiskListener = TestActorRef[CpuDiskListenerMock].underlyingActor

    val timestamp = 0L
    cpuDiskListener.cache should have size 0

    val cpuFormulaValues = CpuFormulaValues(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), timestamp))
    cpuDiskListener.process(cpuFormulaValues)
    cpuDiskListener.cache should contain key timestamp
    cpuDiskListener.cache(timestamp) should equal(Map(Process(123) -> Map("cpu" -> 1.0)))

    val diskFormulaValues = DiskFormulaValues(Energy.fromPower(2), Tick(TickSubscription(Process(123), 1 second), timestamp))
    cpuDiskListener.process(diskFormulaValues)
    cpuDiskListener.cache(timestamp) should equal(Map(Process(123) -> Map("cpu" -> 1.0, "disk" -> 2.0)))

    val anotherTimestamp = 1L
    cpuDiskListener.process(CpuFormulaValues(Energy.fromPower(3), Tick(TickSubscription(Process(123), 1 second), anotherTimestamp)))
    cpuDiskListener.cache(timestamp) should equal(Map(Process(123) -> Map("cpu" -> 1.0, "disk" -> 2.0)))
    cpuDiskListener.cache(anotherTimestamp) should equal(Map(Process(123) -> Map("cpu" -> 3.0)))

    cpuDiskListener.process(DiskFormulaValues(Energy.fromPower(4), Tick(TickSubscription(Process(123), 1 second), anotherTimestamp)))
    cpuDiskListener.cache should have size 1
    cpuDiskListener.cache(anotherTimestamp) should equal(Map(Process(123) -> Map("cpu" -> 3.0, "disk" -> 4.0)))
  }

  @Ignore
  @Test
  def testPid() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.startEnergyModule(_))

    PowerAPI.startMonitoring(Process(27623), 1 second, classOf[CpuDiskListener])
    Thread.sleep((1 minute).toMillis)
    PowerAPI.stopMonitoring(Process(27623), 1 second, classOf[CpuDiskListener])

    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

  @Ignore
  @Test
  def testCurrentPid() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.startEnergyModule(_))

    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuDiskListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuDiskListener])

    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

  @Test
  def testAllPids() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.startEnergyModule(_))

    def getPids = {
      val PSFormat = """^\s*(\d+).*""".r
      Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).lines().toList.map({
        pid =>
          pid match {
            case PSFormat(id) => id.toInt
            case _ => 1
          }
      })
    }

    val pids = scala.collection.mutable.Set[Int]()
    val dur = 1 second
    def udpateMonitoredPids() {
      val currentPids = scala.collection.mutable.Set[Int](getPids: _*)

      val oldPids = pids -- currentPids
      oldPids.foreach(pid => PowerAPI.stopMonitoring(process = Process(pid), duration = dur))
      pids --= oldPids

      val newPids = currentPids -- pids
      newPids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = dur))
      pids ++= newPids
    }

    PowerAPI.startMonitoring(listenerType = classOf[CpuDiskListener])

    val startingTime = System.currentTimeMillis
    while (System.currentTimeMillis - startingTime < (10 seconds).toMillis) {
      udpateMonitoredPids()
      Thread.sleep((250 milliseconds).toMillis)
    }

    PowerAPI.stopMonitoring(listenerType = classOf[CpuDiskListener])

    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

}