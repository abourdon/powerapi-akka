# PowerAPI CPU Formula API module

## Presentation

Base types for each CPU Formula module implementations.

## In

This module reacts to `CPUSensorMessage` messages, typically sent by `fr.inria.powerapi.sensor.sensor-cpu-api` module.

## Out

This module provide the result of CPU formula computation which is represented by the `CPUFormulaMessage` type, and gather:
* the energy value;
* the `Tick` responsible to this computation result
* the string device name, thus `cpu`

## Configuration part

No specific configuration required. Refer to implementations' configuration part.
