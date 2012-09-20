package fr.inria.powerapi.example.demo2.init
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import scalax.io.Resource
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.core.Process
import akka.util.duration._

class CpuListener extends fr.inria.powerapi.listener.cpu.console.CpuListener {
  override def process(cpuFormulaValues: CpuFormulaValues) {
    println(cpuFormulaValues.energy.power)
  }
}

object Demo2Init extends App {

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))

  val PSFormat = """^\s*(\d+).*""".r
  Runtime.getRuntime.exec(Array("mplayer", "-x", "600", "-y", "400", "/home/abourdon/media/movies/demo2.mov"))
  val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-C", "mplayer", "ho", "pid")).getInputStream).lines().toList.map({
    pid =>
      pid match {
        case PSFormat(id) => id.toInt
        case _ => 1
      }
  })

  pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))
  Thread.sleep((2 hours).toMillis)
  pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 500 milliseconds, classOf[CpuListener]))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))

}