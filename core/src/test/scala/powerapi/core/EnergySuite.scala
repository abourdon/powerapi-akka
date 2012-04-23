package powerapi.core
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test
import akka.util.duration._

class EnergySuite extends JUnitSuite with ShouldMatchersForJUnit {

  @Test
  def testFromPower {
    Energy.fromPower(3).power should equal(3)
  }

  @Test
  def testFromJoule {
    Energy.fromJoule(15).power should equal(15)
    Energy.fromJoule(15, 3 seconds).power should equal(5)
  }

}