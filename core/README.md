# PowerAPI Core module

As its name indicates, `Core` module gather all *kernel* functionnalities that will be use by other modules. More particulary, this module defines the whole types used by PowerAPI to define its architecture.

This module also defines the essential `Clock` class, responsible of the periodically emission of the `Tick` message, itself responsible of the process of the PowerAPI business part.
