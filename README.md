# PowerAPI

PowerAPI is a scala-based library for monitoring energy at the process-level. It is based on a modular and asynchronous event-driven architecture using the [Akka library](http://akka.io "Akka library").

PowerAPI differs from existing energy process-level monitoring tool in its pure software, fully customizable and modular aspect which let user to precisely define what he wants to monitor, without any external device.

PowerAPI offers an API which can be used to express request about energy spent by a process, following its hardware resource utilization (in term of CPU, memory, disk, network, etc.).

## Documentation
* [Getting started](#getting-started)
* [Architecture details](#architecture-details)
* [API details](#api-details)
* [Future works](#future-works)
* [License](#license)

<h2 id="getting-started">Getting started</h2>

PowerAPI is completely written in [Scala](http://www.scala-lang.org "Scala language") (v. 2.9.1+), using the [Akka library](http://akka.io "Akka library") (v 2.0.1+). Configuration part is managed by the [Typesafe Config](https://github.com/typesafehub/config "Typesafe Config") (integrated version from the [Akka library](http://akka.io "Akka library")).
PowerAPI project is fully managed by [Maven](http://maven.apache.org "Maven") (v. 3).

### How to acquire it

There are two ways to acquire PowerAPI: With or without Maven repositories. In other words, directly from Maven repositories (to get stable or snapshot versions), or from our Git repository (to get the source code).

#### With Maven repositories

**Stable versions** are available from the [Maven central repository](http://search.maven.org "Maven central repository"). Thus, you just have to put on your `pom.xml` file your desired modules. That's all.

**Snapshot versions** are available from the [OSS Sonatype repository](https://oss.sonatype.org "OSS Sonatype repository"). Thus, you have to add this following repository location:

```xml
<repository>
    <id>OSS Sonatype snapshot repository</id>
	<url>https://oss.sonatype.org/content/repositories/snapshots</url>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```

After that, you just have to put on your `pom.xml` file your desired modules.

#### Without Maven repositories

Without Maven repositories, you have to deal with our Git repository as explain bellow:

To acquire PowerAPI, simply clone it via your Git client:

```bash
git clone git://github.com/abourdon/powerapi-akka.git
```

As PowerAPI is a [Maven](http://maven.apache.org "Maven") managed project, you have to launch the `install` command at the root directory (here, `powerapi_akka_directory`) in order to compile and install it to your local machine:

```bash
cd $powerapi_akka_directory
mvn install
```

**By default, all modules are selected to be installed. Be careful to correctly selecting yours, depending on your environment and the use case you want to do** (see `pom.xml` file at the root directory for more details).

### How to use it

In the way you acquire PowerAPI from [Maven](http://maven.apache.org "Maven") repositories, you can directly use it as a _Jar project_ in your `pom.xml` file.

In the way you acquire PowerAPI from our Git repository, you can navigate to your desired module and use it as a standard [Maven](http://maven.apache.org "Maven") module:

```bash
cd $powerapi_akka_directory/sensors/sensor-cpu-api
mvn test
```

### How to configure it

As said above, configuration part is managed by the [Typesafe Config](https://github.com/typesafehub/config "Typesafe Config"). Thus, be aware to properly configure each module from its `.conf` file(s).

Let's take an example for the `fr.inria.powerapi.formula.formula-cpu-dvfs` module, which implements the PowerAPI CPU `Formula` using the [well-known formula](http://en.wikipedia.org/wiki/CMOS "CPU power formula"), `P = c * f * V * V`, where `c` constant, `f` a frequency and `V` its associated voltage.

To compute this formula, `fr.inria.powerapi.formula.formula-cpu-dvfs` module has to know:
* the CPU [Thermal Design Power](http://en.wikipedia.org/wiki/Thermal_design_power "Thermal Design Power") value;
* the array of frequency/voltage used by the CPU.

This information can be written in its associated configuration file as the following:

```
powerapi {
	cpu {
		tdp = 105
		frequencies = [
			{ value = 1800002, voltage = 1.31 }
			{ value = 2100002, voltage = 1.41 }
			{ value = 2400003, voltage = 1.5 }
		]
	}
}
```

Each module can have its own configuration part. See more details in its associated `README` file.

<h2 id="architecture-details">Architecture details</h2>

PowerAPI is based on a modular and asynchronous event-driven architecture using the [Akka library](http://akka.io "Akka library"). Architecture is centralized around a common [event bus](http://en.wikipedia.org/wiki/Event_monitoring "Event monitoring") where each module can publish/subscribe to sending events. One particularity of this architecture is that each module is in passive state and reacts to events sent by the common event bus.

These modules are organized as follow:

### Core

As its name indicates, `Core` module gather all *kernel* functionnalities that will be used by other modules. More particulary, this module defines the whole types used by PowerAPI to define its architecture.

This module also defines the essential `Clock` class, responsible of the periodically sending of the `Tick` message, later responsible of the process of the PowerAPI business part.

### Sensors

To compute energy spent by a process through its hardware resource utilization, PowerAPI cuts computation in two parts:
1. Hardware resource process utilization monitoring;
2. Computation of the energy implies by the hardware resource process utilization.

Sensor modules or _Sensors_ represents a set of `Sensor`, responsible of the monitoring of hardware resource process utilization. Thus, there is a CPU `Sensor`, a memory `Sensor`, a disk `Sensor` and so on.
As information is given by operating system, there is one `Sensor` implementation by operating system type. Thus there is a CPU Linux `Sensor`, a CPU Windows `Sensor`, and so on.

### Formulae

Set of `Formula`, responsible of the computation of the energy spent by a process on a particular hardware resource (e.g CPU, memory, disk, network), following information provided by its associated `Sensor`.
A `Formula` may depend on the type of the monitored hardware resource. Thus, for the same hardware resource, several `Formula` implementations could exist.

### Listeners

Set of `Listener`, which listen `Formula` events sending by the common event bus. A `Listener` define the actions to do when receiving results from the energy computation (e.g displaying information or producing and submitting information to the common event bus).

### Library

The Library module defines the API that can be used by user to interact with PowerAPI.

<h2 id="api-details">API details</h2>

Process-level energy monitoring is based on a periodically computation that can be expressed via the API. Here there are several examples to describe PowerAPI's API:

### What is the CPU energy spent by the 123 process? Please give me fresh results every 500 milliseconds

Assume that process run under Linux, using a [procfs](http://en.wikipedia.org/wiki/Procfs "Procfs") file system on a _standard_ CPU architecture.
Thus, we need to use the _procfs_ CPU `Sensor` implementation and a given CPU `Formula` implementation, let's say the [DVFS](http://en.wikipedia.org/wiki/Voltage_and_frequency_scaling "DVFS") version. Add to this the desire to display CPU energy spent by process into a console. So we need to:

1. Activate the desired modules:

```scala
Array(
    classOf[fr.inria.powerapi.sensor.cpu.proc.CpuSensor],
    classOf[fr.inria.powerapi.formula.cpu.dvfs.CpuFormula]
).foreach(PowerAPI.startEnergyModule(_))
```

2. Ask to PowerAPI to provide the CPU energy spent by the 123 process, every 500 milliseconds, using a _console Listener_:

```scala
PowerAPI.startMonitoring(
    Process(123),
    500 milliseconds,
    classOf[fr.inria.powerapi.listener.console.CpuListener]
)
```

### Based on the first request, how can I display CPU energy information into a chart too?

Based on the previous code, we simply have to add a new `Listener` which will be able to display CPU energy information into a chart.
PowerAPI integrates a `Listener` using the [JFreeChart](http://www.jfree.org/jfreechart "JFreeChart") Java graph library. So let's add it to the PowerAPI system:

```scala
PowerAPI.startMonitoring(
    listener = classOf[fr.inria.powerapi.listener.jfreechart.CpuListener]
)
```

That's all!

<h2 id="future-works">Future works</h2>

We are working on new _energy modules_ (`Sensor` + `Formula`) development. If you are interested to participate, feel free to contact us via our [GitHub](https://github.com/abourdon/powerapi-akka "GitHub") webpage or mail us at powerapi-user-list@googlegroups.com!

<h2 id="license">License</h2>

Copyright (C) 2012 Inria, University Lille 1.

PowerAPI is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

PowerAPI is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with PowerAPI. If not, see <http://www.gnu.org/licenses/>.
