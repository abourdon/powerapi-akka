# PowerAPI CPU file listener

Display CPU `Formula` results into a file.

## In

Listen to `CPUFormulaValues`, which are typically provided by `fr.inria.powerapi.formula.formula-cpu-api` (inherited) module.

## Out

Display energy information into a file.

## Configuration part

This module allows several configuration:
* Specify the location of the output file;
* Ask to append or rewrite output file when receiving a new `CPUFormulaValues`;
* Just care about power, instead of printing all informations contained into the `CPUFormulaValues`.

For example:
```
powerapi {
	listener {
		cpu-console {
			path = "/path/to/the/output/file"
			append = on
			just-power = on
		}
	}
}
```
