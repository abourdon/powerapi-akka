package fr.inria.powerapi.listener.cpu.file
import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.core.Listener
import scalax.io.Resource
import scalax.io.Line

class CpuListener extends Listener with Configuration {
  lazy val output = Resource.fromFile(filePath)

  def process(cpuFormulaValues: CpuFormulaValues) {
    val toWrite =
      if (justPower) {
        cpuFormulaValues.energy.power.toString
      } else {
        cpuFormulaValues.toString
      }

    if (append) {
      output.append(toWrite + Line.Terminators.NewLine.sep)
    } else {
      output.truncate(0)
      output.write(toWrite)
    }
  }

  def process = {
    case cpuFormulaValues: CpuFormulaValues => process(cpuFormulaValues)
  }

  def messagesToListen = Array(classOf[CpuFormulaValues])
}