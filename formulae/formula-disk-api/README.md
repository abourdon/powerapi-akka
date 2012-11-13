# PowerAPI disk Formula API module

## Presentation

Base types for each disk Formula module implementations.

## In

This module reacts to `DiskSensorMessage` messages, typically sent by `fr.inria.powerapi.sensor.sensor-disk-api` module.

## Out

This module provide the result of disk formula computation which is represented by the `DiskFormulaMessages` type, and gather:
* the energy value;
* the `Tick` responsible to this computation result;
* the device string name, thus "disk"

## Configuration part

No specific configuration required. Refer to implementations' configuration part.
