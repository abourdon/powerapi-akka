# PowerAPI CPU and Disk JFreeChart listener

Display CPU and Disk `Formula`e into a [JFreeChart](http://www.jfree.org/jfreechart "JFreeChart") graph.

## In

Listen to `CPUFormulaValues` and `DiskFormulaValues`, which are typically provided by resp. `fr.inria.powerapi.formula.formula-cpu-api` and `fr.inria.powerapi.formula.formula-disk-api` (inherited) modules.

## Out

Display energy information into a [JFreeChart](http://www.jfree.org/jfreechart "JFreeChart") graph.

## Configuration part

As this module aggregates information from CPU and disk formulae, it has to known the frequency of result displays. This property can be configured as follow :
```
powerapi {
	listener-cpudisk-jfreechart {
	    refresh-rate = 1s
	}
}
```
