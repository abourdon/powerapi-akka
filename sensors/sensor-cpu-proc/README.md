# Procfs filesystem implementation of the PowerAPI CPU Sensor module

## Presentation

Implements the PowerAPI CPU `Sensor` module for systems based on a procfs/sysfs virtual filesystems, typically used by standard Linux distributions.

See also: [Man proc](http://linux.die.net/man/5/proc "proc manual").

## In

Conform to `fr.inria.powerapi.sensor.sensor-cpu-api`.

## Out

Conform to `fr.inria.powerapi.sensor.sensor-cpu-api`.

## Configuration part

To provide CPU information, this module has to know the URL of the time spent under each frequency by the CPU, basically `file:///sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state`, where %? is replacing by the CPU core number. Note that this information can be provided by the [cpufreq-info](http://linux.die.net/man/1/cpufreq-info "cpufreq-info") tool;

For example:
```
powerapi {
	cpu {
		time-in-state = "file:///sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state"
	}
    }
}
```
