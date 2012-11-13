# PowerAPI CPU file listener

Display CPU `Formula` results into a file.

## In

Listen to `CPUFormulaMessage`, which are typically provided by `fr.inria.powerapi.formula.formula-cpu-api` (inherited) module.

## Out

Display energy information into a file.

## Configuration part

This module allows several configuration:
* Specify the path of the output file by indicating its prefix (each output file name is created at runtime by adding a timestamp to the user-defined prefix);
* Ask to append or rewrite output file when receiving a new `CPUFormulaMessage`;
* Just care about power, instead of printing all informations contained into the `CPUFormulaMessage`.

For example:
```
powerapi {
	listener {
		cpu-console {
			out-prefix = "/path/to/the/output/file"
			append = on
			just-power = on
		}
	}
}
```
