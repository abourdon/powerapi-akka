# PowerAPI library

Defines the API that can be use by user to interact with PowerAPI. Here there are several examples to describes PowerAPI's API:

### What is the CPU energy spent by the 123 process? Please give me fresh results every 500 milliseconds

Considering that process run under Linux, using a procfs file system on a *standard* CPU architecture.
Thus, we need to use the procfs CPU `Sensor` implementation and the general CPU `Formula` implementation. Add to this the desire to display CPU energy spent by process into the console. So we need to:

1. Activate desired modules:

``` scala
Array(
    classOf[fr.inria.powerapi.sensor.proc.CpuSensor],
    classOf[fr.inria.powerapi.formula.general.CpuFormula]
).foreach(PowerAPI.startEnergyModule(_))
```

2. Request to PowerAPI system the CPU energy spent by the 123 process, every 500 milliseconds:

``` scala
PowerAPI.startMonitoring(
    Process(123),
    500 milliseconds,
    classOf[fr.inria.powerapi.listener.console.CpuListener]
)
```

### Based on the first request, how can I display CPU energy information into a chart too?

Based on the previous code, we simply have to add a new `Listener` that is able to display CPU energy information into a chart.
PowerAPI integrates a `Listener` using the [JFreeChart](http://www.jfree.org/jfreechart "JFreeChart") implementation. So let's add it to the PowerAPI system:

``` scala
PowerAPI.startMonitoring(
    listener = classOf[fr.inria.powerapi.listener.jfreechart.CpuListener]
)
```

That's all!

