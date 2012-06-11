# General implementation for the PowerAPI CPU Formula module

## Presentation

Implements PowerAPI CPU `Formula` module in weighting each frequency by the time spent by CPU in working under it.

Global CPU energy computation is based on the general well-known formula: `P = c * f * V * V` where `c` constant, `f` a frequency and `V` its associated voltage.

## In

Conform to `fr.inria.powerapi.formula.formula-cpu-api`.

## Out

Conform to `fr.inria.powerapi.formula.formula-cpu-api`.

## Configuration part

To provide CPU energy spent by a process, this module has to know:
* the CPU _Thermal Dissipation Power_ value;
* the array of frequency/voltage used by the CPU

For example:
```
powerapi {
	cpu {
		tdp = 105
		frequencies = [
			{ value = 1800002, voltage = 1.31 }
			{ value = 2100002, voltage = 1.41 }
			{ value = 2400003, voltage = 1.5 }
		]
	}
}
```
