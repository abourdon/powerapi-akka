# PowerSpy PowerAPI Sensor

## Presentation

PowerAPI `Sensor` providing power metrics from a [PowerSpy](http://www.alciom.com/fr/produits/powerspy2.html "PowerSpy") outlet under a Linux platform. See also 

## In

This module reacts to `Tick` messages, typically sent by the core `Clock` class.

## Out

`PowerSpySensorMessage(power: Double)` message which `power` is the `Double` power value from a given time

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
