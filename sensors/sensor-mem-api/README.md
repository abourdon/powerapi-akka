# PowerAPI memory Sensor API module

## Presentation

Base types for each memory `Sensor` module implementations.

## In

This module reacts to `Tick` messages, typically sent by the core `Clock` class.

## Out

This module provide CPU sensor information which is represented by the `MemSensorMessage` type, and gather:
* the process memory resident usage (in percent, from 0 to 1);
* the `Tick` responsible to this monitoring result

## Configuration part

No specific configuration required. Refer to implementations' configuration part.
