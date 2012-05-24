package fr.inria.powerapi.listener.cpu.file
import com.typesafe.config.ConfigException

import scalax.file.Path

trait Configuration extends fr.inria.powerapi.core.Configuration {
  lazy val filePath =
    try {
      conf.getString("powerapi.listener.cpu-console.file-path")
    } catch {
      case ce: ConfigException => Path.createTempFile(
        prefix = "powerapi.listener-cpu-file",
        deleteOnExit = false).path
    }
  lazy val append =
    try {
      conf.getBoolean("powerapi.listener.cpu-console.append")
    } catch {
      case ce: ConfigException => true
    }
  lazy val justPower = try {
    conf.getBoolean("powerapi.listener.cpu-console.just-power")
  } catch {
    case ce: ConfigException => false
  }

}