package fr.inria.powerapi.formula.disk.single
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import akka.actor.ActorSystem
import akka.testkit.TestActorRef

class RateSuiteWrapper extends Configuration with JUnitSuite with ShouldMatchersForJUnit {
  val giga = 1000000000.0
  val mega = 1000000.0
  val byte = 8.0

  def messagesToListen = null
  def acquire = null

  def testFromRateToDoubleSimpleNumber() {
    "1Gb/s".fromRateToDouble should equal(1 * giga / byte)
  }

  def testFromRateToDoubleRealNumberWithDot() {
    "2.3Gb/s".fromRateToDouble should equal(2.3 * giga / byte)
  }

  def testFromRateToDoubleRealNumberWithComma() {
    "2,3Gb/s".fromRateToDouble should equal(2.3 * giga / byte)
  }

  def testFromRateToDoubleUpperMega() {
    "4Mb/s".fromRateToDouble should equal(4 * mega / byte)
  }

  def testFromRateToDoubleLowerMega() {
    "4mb/s".fromRateToDouble should equal(4 * mega / byte)
  }

  def testFromRateToDoubleUpperGiga() {
    "4Gb/s".fromRateToDouble should equal(4 * giga / byte)
  }

  def testFromRateToDoubleLowerGiga() {
    "4gb/s".fromRateToDouble should equal(4 * giga / byte)
  }

  def testFromRateToDoubleBitUnit() {
    "4Gb/s".fromRateToDouble should equal(4 * giga / byte)
  }

  def testFromRateToDoubleByteUnit() {
    "4GB/s".fromRateToDouble should equal(4 * giga)
  }
}

class RateSuite extends JUnitSuite with ShouldMatchersForJUnit {
  implicit val system = ActorSystem("RateSuite")
  val rateSuite = TestActorRef[RateSuiteWrapper].underlyingActor

  @Test
  def testFromRateToDoubleSimpleNumber() {
    rateSuite.testFromRateToDoubleSimpleNumber()
  }

  @Test
  def testFromRateToDoubleRealNumberWithDot() {
    rateSuite.testFromRateToDoubleRealNumberWithDot()
  }

  @Test
  def testFromRateToDoubleRealNumberWithComma() {
    rateSuite.testFromRateToDoubleRealNumberWithComma()
  }

  @Test
  def testFromRateToDoubleUpperMega() {
    rateSuite.testFromRateToDoubleUpperMega()
  }

  @Test
  def testFromRateToDoubleLowerMega() {
    rateSuite.testFromRateToDoubleLowerMega()
  }

  @Test
  def testFromRateToDoubleUpperGiga() {
    rateSuite.testFromRateToDoubleUpperGiga()
  }

  @Test
  def testFromRateToDoubleLowerGiga() {
    rateSuite.testFromRateToDoubleLowerGiga()
  }

  @Test
  def testFromRateToDoubleBitUnit() {
    rateSuite.testFromRateToDoubleBitUnit()
  }

  @Test
  def testFromRateToDoubleByteUnit() {
    rateSuite.testFromRateToDoubleByteUnit()
  }
}