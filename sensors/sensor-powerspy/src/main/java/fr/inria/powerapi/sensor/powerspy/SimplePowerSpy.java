/**
 * Copyright (C) 2012 Inria, University Lille 1.
 *
 * This file is part of PowerAPI.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PowerAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: powerapi-user-list@googlegroups.com.
 */
package fr.inria.powerapi.sensor.powerspy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SimplePowerSpy implements PowerSpy {

	private static final Logger LOG = Logger.getLogger(SimplePowerSpy.class
			.getName());

	public static SimplePowerSpy connect(final String sppUrl) {
		try {
			StreamConnection connection = (StreamConnection) Connector
					.open(sppUrl);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					connection.openDataInputStream()));
			PrintWriter output = new PrintWriter(connection.openOutputStream());

			SimplePowerSpy powerSpy = new SimplePowerSpy(connection);
			powerSpy.setInput(input);
			powerSpy.setOutput(output);
			return powerSpy;
		} catch (IOException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
		} catch (ClassCastException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
		}

		return null;
	}

	protected StreamConnection connection;
	protected Reader input;
	protected Writer output;
	protected ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

	protected SimplePowerSpy(final StreamConnection connection) {
		this.connection = connection;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					close();
				} catch (Exception e) {
					if (LOG.isEnabledFor(Level.WARN)) {
						LOG.warn(e.getMessage());
					}
				}
			}
		});
	}

	protected void setInput(final Reader input) {
		this.input = input;
	}

	protected void setOutput(final Writer output) {
		this.output = output;
	}

	@Override
	public void send(final String message) {
		try {
			ioExecutor.execute(new Runnable() {
				private void send() {
					StringBuilder builder = new StringBuilder();
					builder.append("<");
					builder.append(message);
					builder.append(">");
					try {
						output.write(builder.toString());
						flushOutput();
						if (LOG.isDebugEnabled()) {
							LOG.debug(builder.toString() + " sent");
						}
					} catch (IOException e) {
						if (LOG.isEnabledFor(Level.WARN)) {
							LOG.warn(e.getMessage());
						}
					}
				}

				@SuppressWarnings("unused")
				private void sleep(long timeout) {
					try {
						Thread.sleep(timeout);
					} catch (InterruptedException e) {
						if (LOG.isEnabledFor(Level.WARN)) {
							LOG.warn(e.getMessage());
						}
					}
				}

				@Override
				public void run() {
					send();
					// See specification §3.2 "Error management"
					// Edit: commented because it works fine without
					// sleep(1000);
				}

				@Override
				public String toString() {
					return "SEND (" + message + ")";
				}
			});
		} catch (RejectedExecutionException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage());
			}
		}
	}

	@Override
	public String recv(long timeout, boolean errorOnTimeout) {
		try {
			Future<String> in = ioExecutor.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					StringBuilder buffer = new StringBuilder();
					boolean messageStarted = false;
					char c = '>';
					do {
						c = (char) input.read();
						if (c == '<') {
							messageStarted = true;
						} else if (messageStarted && c != '>') {
							buffer.append(c);
						}
					} while (!(messageStarted && c == '>'));
					return buffer.toString();
				}

				@Override
				public String toString() {
					return "RECV";
				}
			});

			String data = in.get(timeout, TimeUnit.MILLISECONDS);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received " + data);
			}
			return data;
		} catch (InterruptedException e) {
			if (errorOnTimeout && LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received nothing");
			}
			return null;
		} catch (ExecutionException e) {
			if (errorOnTimeout && LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received nothing");
			}
			return null;
		} catch (TimeoutException e) {
			if (errorOnTimeout && LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Received nothing");
			}
			return null;
		} catch (RejectedExecutionException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage());
			}
			return null;
		}
	}

	private boolean closed = false;

	@Override
	public void close() {
		if (!closed) {
			// Stopping monitoring
			stopPowerMonitoring();
			// reset();

			// Stopping I/O tasks
			List<Runnable> remainingTasks = ioExecutor.shutdownNow();
			if (!remainingTasks.isEmpty()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Existing remaing tasks:");
					for (Runnable remainingTask : remainingTasks) {
						LOG.debug(remainingTask);
					}
				}
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("PowerSpy closed");
			}

			// Closing output
			flushOutput();
			try {
				output.close();
			} catch (IOException e) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn(e.getMessage());
				}
			}

			// Closing input
			flushInput();
			try {
				input.close();
			} catch (IOException e) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn(e.getMessage());
				}
			}

			// Closing connection
			try {
				connection.close();
			} catch (IOException e) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn(e.getMessage());
				}
			}
			closed = true;
		}
	}

	private Float uScale = null;

	public Float uScale() {
		if (uScale == null) {
			StringBuilder uCalib = new StringBuilder();
			for (int i = 2; i <= 5; i++) {
				send("V0" + i);
				uCalib.append(recv(DEFAULT_TIMEOUT, true));
			}
			try {
				uScale = IEEE754Utils.fromString(uCalib.toString());
			} catch (NumberFormatException e) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn(e.getLocalizedMessage());
				}
				uScale = null;
			}
		}
		return uScale;
	}

	private Float iScale = null;

	public Float iScale() {
		if (iScale == null) {
			StringBuilder iCalib = new StringBuilder();
			for (int i = 6; i <= 9; i++) {
				send("V0" + i);
				iCalib.append(recv(DEFAULT_TIMEOUT, true));
			}
			try {
				iScale = IEEE754Utils.fromString(iCalib.toString());
			} catch (NumberFormatException e) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn(e.getLocalizedMessage());
				}
				iScale = null;
			}
		}
		return iScale;
	}

	private Float pScale = null;

	public Float pScale() {
		if (pScale == null) {
			if (uScale() != null && iScale() != null) {
				pScale = uScale() * iScale();
			}
		}
		return pScale;
	}

	private Collection<PowerSpyListener> listeners = new HashSet<PowerSpyListener>();

	@Override
	public void addPowerSpyListener(PowerSpyListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePowerSpyListener(PowerSpyListener listener) {
		listeners.remove(listener);
	}

	private void flushOutput() {
		try {
			output.flush();
		} catch (IOException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
		}

	}

	void flushInput() {
		while (recv(DEFAULT_TIMEOUT, false) != null)
			;
	}

	void reset() {
		flushOutput();
		send("R");
		try {
			Thread.sleep(DEFAULT_TIMEOUT);
		} catch (InterruptedException e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn(e.getMessage());
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("PowerSpy reset");
		}
	}

	public void fireDataUpdated(PowerSpyEvent event) {
		for (PowerSpyListener listener : listeners) {
			listener.dataUpdated(event);
		}
	}

	private SimplePowerSpyMonitoring powerMonitoring = null;
	private ExecutorService monitoringExecutor = Executors
			.newSingleThreadExecutor();

	@Override
	public void startPowerMonitoring() {
		if (powerMonitoring == null) {
			powerMonitoring = new SimplePowerSpyMonitoring();
			monitoringExecutor.execute(powerMonitoring);
		}
	}

	@Override
	public void stopPowerMonitoring() {
		if (powerMonitoring != null) {
			powerMonitoring.stopMonitoring();
			List<Runnable> remainingTasks = monitoringExecutor.shutdownNow();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Existing remaining tasks:");
				for (Runnable remainingTask : remainingTasks) {
					LOG.debug(remainingTask);
				}
			}
		}
	}

	class SimplePowerSpyMonitoring implements Runnable {

		private boolean toContinue = true;

		private boolean initPScale() {
			if (pScale() == null) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn("Unable to get uscale");
				}
				return false;
			}
			return true;
		}

		private boolean init() {
			// PowerSpy initialization
			reset();
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
			// Start PowerSpy monitoring in receiving results every 20 averaging
			// periods
			send("J20");
			if (LOG.isDebugEnabled()) {
				LOG.debug("Monitoring started");
			}
		}

		public synchronized void stopMonitoring() {
			setToContinue(false);
			reset();
			flushInput();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Monitoring stopped");
			}
		}

		private Double currentRMS() {
			String recvData = null;
			do {
				recvData = recv(SimplePowerSpy.DEFAULT_TIMEOUT, true);
			} while (recvData == null || recvData.split(" ").length != 5);
			try {
				return Double.valueOf(Integer.valueOf(recvData.split(" ")[2],
						16));
			} catch (NumberFormatException e) {
				if (LOG.isEnabledFor(Level.WARN)) {
					LOG.warn(e.getMessage());
				}
				return Double.valueOf(-1);
			}
		}

		public void monitor() {
			fireDataUpdated(new PowerSpyEvent(currentRMS(), uScale(), iScale()));
		}

		@Override
		public void run() {
			if (init()) {
				startMonitoring();
				while (hasToContinue()) {
					monitor();
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

}
