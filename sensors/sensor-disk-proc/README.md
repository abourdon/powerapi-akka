# Procfs filesystem implementation of the PowerAPI disk Sensor module

## Presentation

Implements the PowerAPI Disk `Sensor` module for systems based on a procfs/sysfs virtual filesystems, typically used by standard Linux distributions.

See also: [Man proc](http://linux.die.net/man/5/proc "proc manual").

## In

Conform to `fr.inria.powerapi.sensor.sensor-disk-api`.

## Out

Conform to `fr.inria.powerapi.sensor.sensor-disk-api`.
__Note that this implementation does not take into account the presence of multiple disk. Thus, there is only one entry in the DiskSensorMessage map__

## Configuration part

To provide disk information, this module has to know the URL of the process io file, basically `file:///proc/%?/io`, where `%?` is at runtime replaced by the _Process IDentifier_, or PID.

For example:
```
powerapi {
	disk {
	    io-file = "file:///proc/%?/io"
	}
}
```
