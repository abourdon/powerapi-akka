package fr.inria.powerapi.sensor.powerspy;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SimplePowerSpyMonitoring implements PowerSpyMonitoring, Runnable {

	private static final Logger LOG = Logger
			.getLogger(SimplePowerSpyMonitoring.class);

	private final SimplePowerSpy simplePowerSpy;

	private boolean toContinue = true;

	/**
	 * @param simplePowerSpy
	 */
	public SimplePowerSpyMonitoring(SimplePowerSpy simplePowerSpy) {
		this.simplePowerSpy = simplePowerSpy;
	}

	private boolean initPScale() {
		if (simplePowerSpy.pScale() == null) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("Unable to get uscale");
			}
			return false;
		}
		return true;
	}

	private boolean init() {
		// PowerSpy initialization
		simplePowerSpy.reset();
		try {
			Thread.sleep(SimplePowerSpy.DEFAULT_TIMEOUT);
		} catch (InterruptedException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
		}

		// Power scale initialization
		if (!initPScale()) {
			return false;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Monitoring initialized");
		}
		return true;
	}

	public void startMonitoring() {
		simplePowerSpy.send("J20");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Monitoring started");
		}
	}

	public synchronized void stopMonitoring() {
		setToContinue(false);
		simplePowerSpy.reset();
		simplePowerSpy.flushInput();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Monitoring stopped");
		}
	}

	private Double getPower() {
		String recvData = null;
		do {
			recvData = simplePowerSpy
					.recv(SimplePowerSpy.DEFAULT_TIMEOUT, true);
		} while (recvData == null || recvData.split(" ").length != 5);
		try {
			// Although it is written in specification §3.4.6
			// "[J] Real Time Parameters command" that corrected RMS power
			// must be scale by (squareroot [ (square of the RMS current
			// returned by fonction) x (Uscale_factory) x (Iscale_factory)
			// ]), we have better results without applying the squareroot.
			// TODO: why?
			return Double.valueOf(Integer.valueOf(recvData.split(" ")[2], 16))
					* simplePowerSpy.pScale();
		} catch (NumberFormatException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
			return Double.valueOf(-1);
		}
	}

	@Override
	public void processPower(Double power) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing power " + power + "...");
		}
		simplePowerSpy.firePowerUpdated(getPower());
	}

	@Override
	public void run() {
		if (init()) {
			startMonitoring();
			while (hasToContinue()) {
				processPower(getPower());
			}
		} else {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("Unable to initialize monitoring");
			}
		}
	}

	public synchronized boolean hasToContinue() {
		return toContinue;
	}

	public synchronized void setToContinue(boolean toContinue) {
		this.toContinue = toContinue;
	}

	@Override
	public String toString() {
		return "PowerSpyMonitoring";
	}

}