# Procfs filesystem implementation of the PowerAPI CPU Sensor module

## Presentation

Implements the PowerAPI CPU `Sensor` module for systems based on a procfs/sysfs virtual filesystems, typically used by standard Linux distributions.

See also: [Man proc](http://linux.die.net/man/5/proc "proc manual").

## In

Conform to `fr.inria.powerapi.sensor.sensor-cpu-api`, without filling the `TimeInStates` field

## Out

Conform to `fr.inria.powerapi.sensor.sensor-cpu-api`.

## Configuration part

To provide CPU information, this module has to know:
* the URL of the global stat file, basically `file:///proc/stat`;
* the URL of the process stat file, basically `file:///proc/%?/stat`, where `%?` is at runtime replaced by the _Process IDentifier_, or PID;

For example:
```
powerapi {
	cpu {
		global-stat = "file:///proc/stat"
		process-stat = "file:///proc/%?/stat"
	}
}
```
