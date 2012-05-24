package fr.inria.powerapi.listener.cpu.file
import java.lang.management.ManagementFactory

import akka.util.duration.intToDurationInt

import org.junit.{ Test, Before, After }
import org.scalatest.junit.{ ShouldMatchersForJUnit, JUnitSuite }

import fr.inria.powerapi.core.{ Clock, Process }
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import scalax.file.Path

trait ConfigurationMock extends Configuration {
  override lazy val filePath = Path.createTempFile(
    prefix = "powerapi.listener-cpu-file",
    deleteOnExit = false).path
}

class CpuListenerMock extends CpuListener with ConfigurationMock

class CpuListenerSuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Before
  def setUp {
    PowerAPI.startModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }

  @Test
  def testCurrentPid {
    val currentPid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt
    PowerAPI.startMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListenerMock])
    Thread.sleep((5 seconds).toMillis)
    PowerAPI.stopMonitoring(Process(currentPid), 500 milliseconds, classOf[CpuListenerMock])
  }

  @After
  def tearDown {
    PowerAPI.stopModules(Array(classOf[Clock], classOf[CpuSensor], classOf[CpuFormula]))
  }
}