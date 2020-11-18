/*
 * PortScanner.java
 *
 *       Created on:  Okt 29, 2020
 *  Last Updated on:  Okt 29, 2020
 *           Author:  Gecko Anton https://github.com/geckoanton
 *
 * Quadrum is a LED-Cube framework and editor.
 * Copyright (C) 2020  geckoanton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package serial;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import ui.editor.controls.SerialControls;

public class PortScanner implements Runnable {
	private static final int SCAN_PERIOD = 250;

	private SerialInterface serialInterface = null;
	private SerialControls serialControls = null;
	private boolean scanLoop = true;

	Thread scanThread = null;

	public void startScan(SerialInterface serialInterface, SerialControls serialControls) {
		if(scanThread != null)
			return;

		this.serialInterface = serialInterface;
		this.serialControls = serialControls;

		scanThread = new Thread(this);
		scanLoop = true;
		scanThread.start();
	}
	public void terminateScan() {
		if(scanThread == null)
			return;
		scanLoop = false;
		scanThread.interrupt();
		scanThread = null;
	}

	@Override
	public void run() {
		if(serialControls == null)
			return;

		while(scanLoop) {
			Platform.runLater(() -> serialControls.updateAvailablePorts(getPorts()));
			try { Thread.sleep(SCAN_PERIOD); } catch(InterruptedException e) {}
		}
	}
	private String[] getPorts() {
		SerialPort[] ports = SerialPort.getCommPorts();
		String[] ret = new String[ports.length];
		boolean currentPortFound = false;
		for(int i = 0; i < ports.length; i++) {
			ret[i] = ports[i].getSystemPortName();
			if(serialInterface != null) {
				if (ret[i].equals(serialInterface.getSerialPort()))
					currentPortFound = true;
			}
		}
		if(!currentPortFound && serialControls != null)
			serialControls.disconnectAction();
		return ret;
	}
}
