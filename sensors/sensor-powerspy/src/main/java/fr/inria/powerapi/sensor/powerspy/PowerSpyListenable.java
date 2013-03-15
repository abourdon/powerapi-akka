package fr.inria.powerapi.sensor.powerspy;

public interface PowerSpyListenable {
	void addPowerSpyListener(PowerSpyListener listener);

	void removePowerSpyListener(PowerSpyListener listener);
}
