# PowerAPI memory Formula API module

## Presentation

Base types for each memory Formula module implementations.

## In

This module reacts to `MemSensorMessage` messages, typically sent by `fr.inria.powerapi.sensor.sensor-mem-api` module.

## Out

This module provide the result of CPU formula computation which is represented by the `MemFormulaMessage` type, and gather:
* the energy value;
* the `Tick` responsible to this computation result
* the string device name, thus `memory`

## Configuration part

No specific configuration required. Refer to implementations' configuration part.
