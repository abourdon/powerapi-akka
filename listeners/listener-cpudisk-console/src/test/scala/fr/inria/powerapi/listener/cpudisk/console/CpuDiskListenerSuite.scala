/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.listener.cpudisk.console
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

class CpuDiskListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Before
  def setUp() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.startEnergyModule(_))
  }

  @Test
  def testCache() {
    implicit val system = ActorSystem("CpuDiskListenerSuite")
    val cpuDiskListener = TestActorRef[CpuDiskListener].underlyingActor

    val timestamp = 0L
    cpuDiskListener.cache.get(timestamp) should be(null)

    val cpuFormulaValues = CpuFormulaValues(Energy.fromPower(1), Tick(TickSubscription(Process(123), 1 second), timestamp))
    cpuDiskListener.process(cpuFormulaValues)
    cpuDiskListener.cache.get(timestamp) should equal(CpuDiskValues(Some(cpuFormulaValues), None))

    val diskFormulaValues = DiskFormulaValues(Energy.fromPower(2), Tick(TickSubscription(Process(123), 1 second), timestamp))
    cpuDiskListener.process(diskFormulaValues)
    cpuDiskListener.cache.get(timestamp) should be(null)
  }

  @Ignore
  @Test
  def testPid() {
    PowerAPI.startMonitoring(Process(20106), 1 second, classOf[CpuDiskListener])
    Thread.sleep((30 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(20106), 1 second, classOf[CpuDiskListener])
  }

  @Test
  def testCurrentPid() {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuDiskListener])
    Thread.sleep((10 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuDiskListener])
  }

  @Ignore
  @Test
  def testAllPids() {
    val PSFormat = """^\s*(\d+).*""".r
    val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-A")).getInputStream).lines().toList.map({
      pid =>
        pid match {
          case PSFormat(id) => id.toInt
          case _ => 1
        }
    })

    PowerAPI.startMonitoring(listenerType = classOf[CpuDiskListener])
    pids.foreach(pid => PowerAPI.startMonitoring(process = Process(pid), duration = 500 milliseconds))

    Thread.sleep((10 seconds).toMillis)

    PowerAPI.stopMonitoring(listenerType = classOf[CpuDiskListener])
    pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[CpuDiskListener]))
  }

  @After
  def tearDown() {
    Array(
      classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
      classOf[fr.inria.powerapi.formula.cpu.general.CpuFormula],
      classOf[fr.inria.powerapi.sensor.disk.proc.DiskSensor],
      classOf[fr.inria.powerapi.formula.disk.single.DiskFormula]).foreach(PowerAPI.stopEnergyModule(_))
  }

}