# General implementation for the PowerAPI memory Formula module

## Presentation

Implements PowerAPI memory `Formula` module giving memory energy of a specified process in making the ratio between the read/write power by the real process usage.

## In

Conform to `fr.inria.powerapi.formula.formula-mem-api`.

## Out

Conform to `fr.inria.powerapi.formula.formula-mem-api`.

## Configuration part

To provide disk energy spent by a process, this module has to know:
* the power consumed by the __total__ memory (in case of several RAM sticks, just make an addition of all of them) during the reading process;
* the power consumed by the __total__ memory (in case of several RAM sticks, just make an addition of all of them) during the writing process;

For example:
```
powerapi {
	mem {
		read-power = 5.2
		write-power = 5.6
	}
}
```
