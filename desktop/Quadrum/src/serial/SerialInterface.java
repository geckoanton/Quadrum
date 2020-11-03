/*
 * SerialInterface.java
 *
 *       Created on:  Okt 29, 2020
 *  Last Updated on:  Okt 29, 2020
 *           Author:  Anton Gecko https://github.com/antongecko
 *
 * Quadrum is a LED-Cube framework and editor.
 * Copyright (C) 2020  antongecko
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

import project.Frame;
import project.Project;
import ui.editor.controls.SerialControls;

public class SerialInterface implements Runnable {

	private static final long ZERO_FRAME_DELAY = 100;

	private ProtocolSerial protocolSerial = new ProtocolSerial();

	private SerialControls serialControls = null;
	private Thread serialThread = null;
	private String serialPort = null;

	private boolean toggleZeroFrame = false;

	public void connect(SerialControls serialControls) {
		if(protocolSerial.isConnected() || serialThread != null)
			return;

		this.serialControls = serialControls;

		serialThread = new Thread(this);
		serialThread.start();
	}
	public void disconnect() {
		if(serialThread == null)
			return;
		serialThread.interrupt();
		serialThread = null;
		serialControls.connectedMode(false);
	}
	public void sendFrame(Frame frame) {
		if(!protocolSerial.isConnected() || serialThread == null)
			return;

		protocolSerial.streamFrame(frame);
	}
	public void setToggleZeroFrame(boolean toggle) {
		toggleZeroFrame = toggle;
	}
	public void sendZeroFrame(int frameSide) {
		if(!toggleZeroFrame)
			return;

		displayStreamedFrame();
		sendFrame(new Frame(frameSide));
		try { Thread.sleep(ZERO_FRAME_DELAY); }
		catch (InterruptedException e) {}
		displayStreamedFrame();
		toggleZeroFrame = false;
	}
	public void displayStreamedFrame() {
		if(!protocolSerial.isConnected() || serialThread == null)
			return;
		protocolSerial.displayFrame();
	}

	public void startStreamingProject(Project project) {
		if(serialControls == null || !protocolSerial.isConnected())
			return;

		if(protocolSerial.getCubeSide() == project.getSide()) {
			serialControls.printlnConsole("streaming project " + project.getName());
		}
		else {
			serialControls.printlnConsole("warning, current project " + project.getName() + " with cube side of " + project.getSide() +
					" does not match connected device's cube side of " + protocolSerial.getCubeSide());
		}
	}
	public void endStreamingProject(Project project) {
		if(serialControls == null || !protocolSerial.isConnected())
			return;

		if(protocolSerial.getCubeSide() == project.getSide()) {
			serialControls.printlnConsole("project stream ended");
		}
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}
	public String getSerialPort() {
		return serialPort;
	}

	public boolean isConnected() {
		return protocolSerial.isConnected();
	}

	@Override
	public void run() {
		if(serialControls == null)
			return;

		serialControls.clearConsole();

		if(serialPort != null) {
			serialControls.connectedMode(false);
			serialControls.printlnConsole("connecting to port '" + serialPort + "'...");

			boolean connectedFlag = false;
			switch (protocolSerial.initSerial(serialPort)) {
				case CONNECTION_SUCCEEDED:
					serialControls.printlnConsole("loaded cube side " + protocolSerial.getCubeSide());
					serialControls.printlnConsole("number of color channels " + protocolSerial.getChannelCount());
					serialControls.printlnConsole("channel resolution " + protocolSerial.getChannelSize() + " bits");
					serialControls.printlnConsole("connected");
					serialControls.connectedMode(true);
					connectedFlag = true;
					break;
				case CONNECTION_FAILED:
					serialControls.printlnConsole("connection failed");
					break;
				case CONNECTION_TIMEOUT:
					serialControls.printlnConsole("connection timed out");
					break;
				case NOT_RESPONDING_PROPERLY:
					serialControls.printlnConsole("received improper response");
					break;
			}

			try {
				synchronized (this) {
					if(connectedFlag)
						wait();
				}
			}
			catch(InterruptedException e) {}

			if(connectedFlag) {
				serialControls.printlnConsole("disconnected, a total of " + protocolSerial.getFailedFrames() + " failed frames were detected");
			}
		}
		else {
			serialControls.printlnConsole("no serial port selected");
		}

		protocolSerial.terminateConnection();

		disconnect();
	}
}
