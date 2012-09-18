package fr.inria.powerapi.sensor.disk.atop
import java.io.IOException
import fr.inria.powerapi.sensor.disk.api.DiskSensorValues
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import fr.inria.powerapi.core.Listener
import fr.inria.powerapi.core.Clock
import fr.inria.powerapi.core.TickIt
import fr.inria.powerapi.core.UnTickIt
import fr.inria.powerapi.core.TickSubscription
import scalax.io.Resource
import java.io.FileInputStream
import java.net.URL
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import scala.util.Properties
import org.junit.Test
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.actor.Props
import akka.util.duration._

trait ConfigurationMock extends Configuration {
  lazy val basedir = new URL("file", Properties.propOrEmpty("basedir"), "")
  override lazy val processStatPath = new URL(basedir, "/src/test/resources/proc/%?/stat").toString
}

class DiskSensorMock extends DiskSensor with ConfigurationMock

class DiskSensorReceiver extends Listener {
  val receivedData = collection.mutable.Map[String, (Long, Long)]()

  def messagesToListen = Array(classOf[DiskSensorValues])

  def acquire = {
    case diskSensorValues: DiskSensorValues => process(diskSensorValues)
  }

  def process(diskSensorValues: DiskSensorValues) {
    receivedData ++= diskSensorValues.rw
  }
}

class DiskSensorSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Test
  def testReadAndWrite() {
    implicit val system = ActorSystem("DiskSensorSuite")
    val diskSensor = TestActorRef[DiskSensorMock].underlyingActor

    diskSensor.readAndWrite(Process(123)) should equal((1, 3))
  }

  @Test
  def testTick() {
    implicit val system = ActorSystem("DiskSensorSuite")
    val diskSensor = system.actorOf(Props[DiskSensorMock])
    val diskSensorReceiver = TestActorRef[DiskSensorReceiver]
    val clock = system.actorOf(Props[Clock])
    system.eventStream.subscribe(diskSensor, classOf[Tick])
    system.eventStream.subscribe(diskSensorReceiver, classOf[DiskSensorValues])

    clock ! TickIt(TickSubscription(Process(123), 10 seconds))
    Thread.sleep(1000)
    clock ! UnTickIt(TickSubscription(Process(123), 10 seconds))

    diskSensorReceiver.underlyingActor.receivedData should have size 1
    diskSensorReceiver.underlyingActor.receivedData("n/a") should equal((1.0, 3.0))
  }
}