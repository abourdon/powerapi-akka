# ATOP linux kernel implementation of the PowerAPI disk Sensor module

## Presentation

Implements the PowerAPI disk Sensor module for systems based on an ATOP linux kernel,

See also: [ATOP website](http://www.atoptool.nl "ATOP website").

## In

Conform to `fr.inria.powerapi.sensor.sensor-disk-api`.

## Out

Conform to `fr.inria.powerapi.sensor.sensor-disk-api`.
__Note that this implementation does not take into account the presence of multiple disk. Thus, there is only one entry in the DiskSensorMessage map__

## Configuration part

To provide CPU information, this module has to know the URL of the process stat file, basically `file:///proc/%?/stat`, where `%?` is at runtime replaced by the _Process IDentifier_, or PID.

For example:
```
powerapi {
	disk {
	    stat-file = "file:///proc/%?/stat"
	}
}
```
