# General implementation for the PowerAPI disk Formula module

## Presentation

Implements PowerAPI disk `Formula` module giving disk energy of a specified process in making the ratio between the read/write power by the maximum read/write rate of the physical disk.

__Note that this implementation doesn't take into account that a process could use different physical disks. It assumes that all processes use the primary disk__

## In

Conform to `fr.inria.powerapi.formula.formula-disk-api`.

## Out

Conform to `fr.inria.powerapi.formula.formula-disk-api`.

## Configuration part

To provide disk energy spent by a process, this module has to know:
* the power consumed by the disk when the reading process;
* the maximum rate during the reading process allowed by the disk;
* the power consumed by the disk when writing process;
* the maximum rate during the writing process allowed by the disk.

For example:
```
powerapi {
	disk {
		read-power = 2.1
		read-rate = 100MB/s
		write-power = 2.2
		write-rate = 90MB/s
	}
}
```
