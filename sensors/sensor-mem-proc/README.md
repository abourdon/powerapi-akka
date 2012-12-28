# Procfs filesystem implementation of the PowerAPI memory Sensor module

## Presentation

Implements the PowerAPI memory `Sensor` module for systems based on a procfs/sysfs virtual filesystems, typically used by standard Linux distributions.

See also: [Man proc](http://linux.die.net/man/5/proc "proc manual").

## In

Conform to `fr.inria.powerapi.sensor.sensor-mem-api`.

## Out

Conform to `fr.inria.powerapi.sensor.sensor-mem-api`.

## Configuration part

To provide memory information, this module has to know:
* the URL of the process `status` file, basically `file:///proc/%?/status`, where `%?` is at runtime replaced by the _Process IDentifier_, or PID;
* the URL of the global `meminfo` file, basically `file:///proc/meminfo`.

For example:
```
powerapi {
	disk {
	    process-status = "file:///proc/%?/status"
	    global-meminfo = "file:///proc/meminfo"
	}
}
```
