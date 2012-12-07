# Time in states implementation of the PowerAPI CPU Sensor module (based on sensor-cpu-sigar)

## Presentation

Extends the `fr.inria.powerapi.sensor.cpu.sensor-cpu-sigar` module in providing time spent for each CPU frequency, useful for computation taking into account the DVFS.

This `Sensor` module use information provided by the [cpufreq-info](http://linux.die.net/man/1/cpufreq-info "cpufreq-info") tool under the procfs/sysfs virtual filesystems, typically used by standard Linux distributions.

See also: [Man proc](http://linux.die.net/man/5/proc "proc manual").

## In

Conform to `fr.inria.powerapi.sensor.sensor-cpu-api`.

## Out

Conform to `fr.inria.powerapi.sensor.sensor-cpu-api`.

## Configuration part

This module has to know the URL of the time spent under each frequency by the CPU, basically `file:///sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state`, where %? is replacing by the CPU core number. Note that this information can be provided by the [cpufreq-info](http://linux.die.net/man/1/cpufreq-info "cpufreq-info") tool;

For example:
```
powerapi {
	cpu {
		time-in-state = "file:///sys/devices/system/cpu/cpu%?/cpufreq/stats/time_in_state"
	}
    }
}
```
