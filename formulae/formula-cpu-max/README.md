# Implementation of the PowerAPI CPU Formula module by maximum frequency and voltage used

## Presentation

Implements PowerAPI CPU Formula module at the maximum frequency and voltage used by the CPU principally represented by its [Thermal Design Power](http://en.wikipedia.org/wiki/Thermal_design_power "Thermal Design Power").

## In

Conform to `fr.inria.powerapi.formula.formula-cpu-api`.

## Out

Conform to `fr.inria.powerapi.formula.formula-cpu-api`.

## Configuration part

To provide CPU energy spent by a process, this module has to know the CPU [Thermal Design Power](http://en.wikipedia.org/wiki/Thermal_design_power "Thermal Design Power") value.

For example:
```
powerapi {
	cpu {
		tdp = 105
	}
}
```
