# PowerSpy PowerAPI Formula

## Presentation

PowerAPI `Formula` providing power metrics from a [PowerSpy](http://www.alciom.com/fr/produits/powerspy2.html "PowerSpy") outlet under a Linux platform.

## In

This module reacts to `PowerSpySensorMessage` messages, typically sent by the core `Clock` class.

## Out

`PowerSpyFormulaMessage(energy: Double, tick: Tick, device: String)` message which:
* `energy` is the associated `fr.inria.powerapi.Energy` value of the PowerSpy
* `tick` is the nearest current `Tick`. Nearest because we cannot perfectly synchronize the PowerSpy with PowerAPI. So when a new value comes from the PowerSpy, the nearest PowerAPI's `Tick` value is associated to it ;
* `device` is always set to `powerspy`.

## Configuration

No specific configuration.
