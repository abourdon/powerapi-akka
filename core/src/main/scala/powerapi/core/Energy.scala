package powerapi.core
import akka.util.Duration
import akka.util.duration._

case class Energy private (power: Double)

object Energy {
  def fromPower(power: Double) = new Energy(power)

  def fromJoule(joule: Double, duration: Duration = 1 second) = new Energy(joule / duration.toSeconds)
}