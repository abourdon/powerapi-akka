# PowerAPI CPU Sensor API module

## Presentation

Base types for each CPU `Sensor` module implementations.

## In

This module reacts to `Tick` messages, typically sent by the core `Clock` class.

## Out

This module provide CPU sensor information which is represented by the `CPUSensorMessage` type, and gather:
* the time spent under each frequency by the CPU;
* the CPU elapsed time;
* the process CPU elapsed time;
* the `Tick` responsible to this monitoring result

## Configuration part

No specific configuration required. Refer to implementations' configuration part.
