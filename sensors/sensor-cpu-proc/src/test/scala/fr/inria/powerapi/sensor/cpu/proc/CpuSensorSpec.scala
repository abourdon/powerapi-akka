package fr.inria.powerapi.sensor.cpu.proc
import java.net.URL

import scala.util.Properties

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.ShouldMatchersForJUnit
import org.scalatest.FlatSpec

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.util.duration.intToDurationInt
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.TickSubscription
import fr.inria.powerapi.sensor.cpu.api.TimeInStates

@RunWith(classOf[JUnitRunner])
class CpuSensorSpec extends FlatSpec with ShouldMatchersForJUnit {

  trait ConfigurationMock extends Configuration {
    override lazy val timeInStatePath = new URL(
      new URL("file", Properties.propOrEmpty("basedir"), ""),
      "/src/test/resources/sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state").toString
  }

  implicit val system = ActorSystem("cpusensorsuite")
  implicit val tick = Tick(TickSubscription(Process(123), 1 second))
  val cpuSensor = TestActorRef(new CpuSensor with ConfigurationMock {
    override lazy val cores = 4
  })

  "Frequencies' time in states" should "be correctly read from the dedicated system file" in {
    cpuSensor.underlyingActor.frequencies.timeInStates should equal(Map(
      4000000 -> 16,
      3000000 -> 12,
      2000000 -> 8,
      1000000 -> 4
    ))
  }

  "Frequencies' cache" should "be correctly updated during process phase" in {
    cpuSensor.underlyingActor.frequencies.cache should have size 0

    cpuSensor.underlyingActor.frequencies.process(tick.subscription)
    cpuSensor.underlyingActor.frequencies.cache should have size 1
    cpuSensor.underlyingActor.frequencies.cache should contain key tick.subscription
    cpuSensor.underlyingActor.frequencies.cache.get(tick.subscription).get should equal(TimeInStates(Map(
      4000000 -> 16,
      3000000 -> 12,
      2000000 -> 8,
      1000000 -> 4
    )))
  }

}