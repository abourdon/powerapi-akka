package fr.inria.powerapi.sensor.powerspy;

import java.io.Closeable;

public interface BluetoothDevice extends Closeable {

	public void send(String message);

	public String recv(long timeout, boolean errorOnTimeout);
}
