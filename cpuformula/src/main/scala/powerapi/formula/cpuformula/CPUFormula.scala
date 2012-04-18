package powerapi.formula.cpuformula
import akka.actor.Actor
import powerapi.core.Configuration

case object Tdp
case object NumberOfCores
case object Frequencies
case class Frequency(value: Int, voltage: Double)

class CPUFormula extends Actor with Configuration {
  // Environment specific values (from the configuration file)
  lazy val tdp = fromConf("tdp") { node => (node \\ "@value").text.toDouble }(0)
  lazy val numberOfCores = fromConf("numberOfCores") { node => (node \\ "@value").text.toInt }(0)
  lazy val frequencies = fromConf("frequency") { node => Frequency((node \\ "@value").text.toInt, (node \\ "@voltage").text.toDouble) }

  def receive = {
    case Tdp => tdp
    case NumberOfCores => numberOfCores
    case Frequencies => frequencies
  }
}