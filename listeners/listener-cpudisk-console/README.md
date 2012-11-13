# PowerAPI CPU and disk console listener

## Presentation

Make the aggregation between CPU and disk `Formula` results and display them into the console.

## In

Listen to:
* `CpuFormulaMessage`, which are typically provided by `fr.inria.powerapi.formula.formula-cpu-api` (inherited) module.
* `DiskFormulaMessage`, which are typically provided by `fr.inria.powerapi.formula.formula-disk-api` (inherited) module.

## Out

Display energy information into the console.

## Configuration part

As this module aggregates information from CPU and disk formulae, it has to known the frequency of result displays. This property can be configured as follow :
```
powerapi {
	listener-cpudisk-console {
	    refresh-rate = 1s
	}
}
```
