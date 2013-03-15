package fr.inria.powerapi.sensor.powerspy;

public interface PowerSpy extends BluetoothDevice, PowerSpyListenable {

	static final long DEFAULT_TIMEOUT = 3000;

	void startPowerMonitoring();

	void stopPowerMonitoring();
}
