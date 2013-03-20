package fr.inria.powerapi.sensor.powerspy

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Test

class PowerSpySensorDelegateMessageSuite extends JUnitSuite with ShouldMatchersForJUnit {
  @Test
  def testSum() {
    PowerSpySensorDelegateMessage(1.0, 1.0f, 1.0f) + PowerSpySensorDelegateMessage(1.0, 1.0f, 1.0f) should equal(PowerSpySensorDelegateMessage(1.0 + 1.0, 1.0f + 1.0f, 1.0f + 1.0f))
  }

  @Test
  def testDivide() {
    PowerSpySensorDelegateMessage(3.0, 3.0f, 3.0f) / 2 should equal(PowerSpySensorDelegateMessage(3.0 / 2, 3.0f / 2, 3.0f / 2))
  }

  @Test
  def testAvg() {
    PowerSpySensorDelegateMessage.avg(PowerSpySensorDelegateMessage(1.0, 1.0f, 1.0f) :: PowerSpySensorDelegateMessage(2.0, 2.0f, 2.0f) :: PowerSpySensorDelegateMessage(3.0, 3.0f, 3.0f) :: Nil) should equal(PowerSpySensorDelegateMessage((1.0 + 2.0 + 3.0) / 3, (1.0f + 2.0f + 3.0f) / 3, (1.0f + 2.0f + 3.0f) / 3))
  }
}