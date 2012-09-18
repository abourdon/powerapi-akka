package fr.inria.powerapi.sensor.disk.atop
import java.io.IOException
import fr.inria.powerapi.sensor.disk.api.DiskSensorValues
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.core.Tick
import scalax.io.Resource
import java.io.FileInputStream
import java.net.URL

trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val processStatPath = load { _.getString("powerapi.disk.process-stat") }("file:///proc/%?/stat")
}

class DiskSensor extends fr.inria.powerapi.sensor.disk.api.DiskSensor with Configuration {

  def readAndWrite(implicit process: Process) =
    try {
      // FIXME: Due to Java JDK bug #7132461, there is no way to apply buffer to procfs files and thus, directly open stream from the given URL.
      // Then, we simply read these files thanks to a FileInputStream in getting those local path
      val line = Resource.fromInputStream(new FileInputStream(new URL(processStatPath replace ("%?", process.pid.toString)).getPath)).lines().toIndexedSeq(1).toString.split("\\s")
      // Read bytes, Write bytes
      (line(0).toLong, line(2).toLong)
    } catch {
      case ioe: IOException => {
        log.warning("i/o exception: " + ioe.getMessage)
        (0: Long, 0: Long)
      }
    }

  def process(tick: Tick) {
    publish(DiskSensorValues(Map("n/a" -> readAndWrite(tick.subscription.process)), tick))
  }
}