# PowerAPI library

Defines the API that can be use by user to interact with PowerAPI. Here there are several examples to describes PowerAPI's API:

### What is the CPU energy spent by the 123 process? Please give me fresh results every 500 milliseconds

Considering that process run under Linux, using a procfs file system on a *standard* CPU architecture.
Thus, we need to use the procfs CPU `Sensor` implementation and the a given CPU `Formula` implementation, let's say the [DVFS](http://en.wikipedia.org/wiki/Voltage_and_frequency_scaling "DVFS") version. Add to this the desire to display CPU energy spent by process into the console. So we need to:

1. Activate desired modules:

``` scala
Array(
    classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
    classOf[fr.inria.powerapi.formula.cpu.dvfs.CpuFormula]
).foreach(PowerAPI.startEnergyModule(_))
```

2. Ask to PowerAPI to provide the CPU energy spent by the 123 process, every 500 milliseconds, using a _console Reporter_ and aggregating results by timestamp produced every 500 milliseconds:

```scala
PowerAPI.startMonitoring(
    process = Process(123),
    duration = 500 milliseconds,
    processor = classOf[fr.inria.powerapi.processor.TimestampAggregator],
    listener = classOf[fr.inria.powerapi.reporter.ConsoleReporter],
)
```

Note that we use `listener` as parameter instead of `reporter` for legacy reasons.

### Based on the first request, how can I display CPU energy information into a chart too?

Based on the previous code, we simply have to add a new `Reporter` which will be able to display CPU energy information into a chart.
PowerAPI integrates a `Reporter` using the [JFreeChart](http://www.jfree.org/jfreechart "JFreeChart") Java graph library. So let's add it to the PowerAPI system:

```scala
PowerAPI.startMonitoring(
    listener = classOf[fr.inria.powerapi.reporter.JFreeChartReporter]
)
```

That's all!
