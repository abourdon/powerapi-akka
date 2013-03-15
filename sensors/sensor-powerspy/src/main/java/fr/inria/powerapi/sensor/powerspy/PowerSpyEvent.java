package fr.inria.powerapi.sensor.powerspy;

public final class PowerSpyEvent {

	private final Double power;

	public PowerSpyEvent(final Double power) {
		this.power = power;
	}

	public Double getPower() {
		return power;
	}

}
