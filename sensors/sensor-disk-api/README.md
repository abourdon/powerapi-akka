# PowerAPI disk Sensor API module

## Presentation

Base types for each disk `Sensor` module implementations.

## In

This module reacts to `Tick` messages, typically sent by the core `Clock` class.

## Out

This module provide disk sensor information which is represented by the `DiskSensorValues` type, and gather for each physical disk in the system, the couple (read, written) bytes causes to be fetched by from the storage level

## Configuration part

No specific configuration required. Refer to implementations' configuration part.
