# PowerAPI file reporter

Display `AggregatedMessage` into a file.

## In

Listen to `AggregatedMessage`, which are typically provided by `fr.inria.powerapi.listener.listener-aggregator` module.

## Out

Display energy information into a file.

## Configuration part

This module has to know the path prefix of the output file.

For example:
```
powerapi {
	listener {
		file {
			prefix = "/path/to/the/output/file/prefix"
		}
	}
}
```
