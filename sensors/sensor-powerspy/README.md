# PowerSpy PowerAPI Sensor

## Presentation

PowerAPI `Sensor` providing power metrics from a [PowerSpy](http://www.alciom.com/fr/produits/powerspy2.html "PowerSpy") outlet under a Linux platform. 

## In

This module reacts to `Tick` messages, typically sent by the core `Clock` class.

## Out

`PowerSpySensorMessage(currentRMS: Double, uScale: Float, iScale: Float, tick: Tick)` message which:
* `currentRMS` is the square of the current RMS (Root Mean Square, see documentation) ;
* `uScale` is the factory correction voltage coefficient ;
* `iScale` is the factory correction amperage coefficient ;
* `tick` is the nearest current `Tick`. Nearest because we cannot perfectly synchronize the PowerSpy with PowerAPI. So when a new value comes from the PowerSpy, the nearest PowerAPI's `Tick` value is associated to it.

## Configuration

This module has to know adress of the given [PowerSpy](http://www.alciom.com/fr/produits/powerspy2.html "PowerSpy"), represented by a BtSPP (Bluetooth Serial Port Protocol) URL

For example:
```
powerapi {
	sensor {
		powerspy {
			spp-url = "btspp://the-powerspy-mac-address;some=value"
		}
	}
}
```
