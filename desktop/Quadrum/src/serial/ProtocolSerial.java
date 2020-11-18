/*
 * ProtocolSerial.java
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
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import project.Frame;
import java.util.Arrays;

public class ProtocolSerial {
	private static final int SERIAL_SPEED = 115200,
			STREAM_SIZE = 8;
	private static final int INIT_RESPONSE_WAIT = 150,	// wait 150 ms for mcu to respond on init packet request
			INIT_RETRIES = 8,	// retry times until giving up
			INIT_DATA_SIZE = 4;
	private static final int ACKNOWLEDGE_PACKET_WAIT = 10,
			DISPLAY_RESEND_TIME = 50;

	private SerialPort serialPort = null;

	private int cubeSide, channelCount, channelSize;

	private int failedFrames, corruptedPackets;
	private boolean failedFramesIncreased = false;

	private byte[] voxelData = null;
	private int voxelDataStride;

	private boolean displayFrame = false;
	private long displayResendStartTime = 0;

	// protocol packet constants
	private static final int INIT_REQUEST_PACKET = 127,
			INIT_START_PACKET = 120,
			INIT_ACKNOWLEDGE_PACKET = 113;
	private static final int STREAM_REQUEST_PACKET = 63, TERMINATE_COMMUNICATION_PACKET = 0,
			STREAM_START_COMPRESSED_PACKET = 32, STREAM_START_RAW_PACKET = 31, DISPLAY_PACKET = 24,
			STREAM_END_PACKET = 248,
			STREAM_ACKNOWLEDGE_PACKET = 240, STREAM_CORRUPTED_PACKET = 224,
			FRAME_DISPLAYED_PACKET = 217, FRAME_FAILED_PACKET = 210;

	public synchronized SerialStatus initSerial(String port) {
		terminateConnection();

		serialPort = SerialPort.getCommPort(port);
		serialPort.setComPortParameters(SERIAL_SPEED, 8, 1, 0);
		serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

		if (!serialPort.openPort()) {
			terminateConnection();
			return SerialStatus.CONNECTION_FAILED;
		}

		SerialStatus currentSerialStatus = SerialStatus.CONNECTION_TIMEOUT;
		for(int i = 0; i < INIT_RETRIES; i++) {	// retry connection a few times before giving up
			clearSerialBytes();
			sendSerialBytes((byte) INIT_REQUEST_PACKET);

			sleep(INIT_RESPONSE_WAIT);

			byte[] initData = readSerialBytes();
			if(initData.length == INIT_DATA_SIZE) {
				if(initData[0] == (byte) INIT_START_PACKET) {
					if(!setCubeParams(initData[1], initData[2], initData[3])) {
						currentSerialStatus = SerialStatus.NOT_RESPONDING_PROPERLY;
						break;
					}
					sendSerialBytes((byte) INIT_ACKNOWLEDGE_PACKET);
					currentSerialStatus = SerialStatus.CONNECTION_SUCCEEDED;
					break;
				}
				else {
					currentSerialStatus = SerialStatus.NOT_RESPONDING_PROPERLY;
				}
			}
			else if(initData.length > 0) {
				currentSerialStatus = SerialStatus.NOT_RESPONDING_PROPERLY;
			}
		}

		if(currentSerialStatus == SerialStatus.CONNECTION_SUCCEEDED) {
			serialPort.addDataListener(new SerialPortDataListener() {	// set up serial listener to process frame streaming
				public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
				public void serialEvent(SerialPortEvent event) { serialDataAvailable(); }
			});
		}
		else {
			terminateConnection();
		}

		return currentSerialStatus;
	}
	public synchronized void terminateConnection() {
		if(serialPort == null)
			return;

		serialPort.removeDataListener();
		serialPort.closePort();
		serialPort = null;

		cubeSide = -1;
		channelCount = -1;
		channelSize = -1;

		corruptedPackets = 0;
		failedFrames = 0;

		if(voxelData != null)
			Arrays.fill(voxelData, (byte) 0);
		voxelData = null;
		voxelDataStride = 0;
	}
	public synchronized void streamFrame(Frame frame) {
		if(frame == null || serialPort == null || cubeSide != frame.getSide())
			return;

		frame.updateVoxelData(voxelData, channelCount, channelSize);
		failedFramesIncreased = false;
	}
	public synchronized void displayFrame() {
		displayFrame = true;
		displayResendStartTime = System.currentTimeMillis();
	}
	public synchronized boolean isConnected() {
		return serialPort != null;
	}

	private synchronized void serialDataAvailable() {
		if(serialPort == null)
			return;

		byte[] availableBytes = readSerialBytes();
		if(availableBytes.length > 0) {
			byte latestByte = availableBytes[availableBytes.length - 1];

			if(latestByte == (byte) STREAM_REQUEST_PACKET) {
				stream();
			}
			else if(latestByte == (byte) TERMINATE_COMMUNICATION_PACKET) {
				terminateConnection();
			}
		}
	}
	private void stream() {
		if(displayFrame) {
			if(System.currentTimeMillis() > displayResendStartTime + DISPLAY_RESEND_TIME)
				displayFrame = false;

			sendSerialBytes((byte) DISPLAY_PACKET);
			voxelDataStride = 0;

			long startTime = System.currentTimeMillis();
			while(System.currentTimeMillis() < startTime + ACKNOWLEDGE_PACKET_WAIT) {
				if(serialPort.bytesAvailable() > 0) {
					byte displayStatus = readSerialBytes()[0];

					if(displayStatus == (byte) FRAME_DISPLAYED_PACKET) {
						// frame successfully displayed
						displayFrame = false;
					}
					else if(displayStatus == (byte) FRAME_FAILED_PACKET && !failedFramesIncreased) {
						failedFrames++;	// increment this when frame displayed is never received!
						failedFramesIncreased = true;
					}

					break;
				}
			}

			return;
		}

		StreamData compressedStreamData = getCompressedStream();
		StreamData rawStreamData = getRawStream();
		int strideIncrease = 0;

		if(compressedStreamData.getTotalChannelStride() > rawStreamData.getTotalChannelStride()) {	// compressed stream
			sendSerialBytes((byte) STREAM_START_COMPRESSED_PACKET);
			sendSerialBytes(compressedStreamData.getData());
			strideIncrease = compressedStreamData.getTotalChannelStride();
		}
		else {	// raw stream
			sendSerialBytes((byte) STREAM_START_RAW_PACKET);
			sendSerialBytes(rawStreamData.getData());
			strideIncrease = rawStreamData.getTotalChannelStride();
		}

		sendSerialBytes((byte) STREAM_END_PACKET);
		sendSerialBytes((byte) 1);	// (future) checksum

		// wait for stream acknowledge before increasing totalChannelStride
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() < startTime + ACKNOWLEDGE_PACKET_WAIT) {
			if(serialPort.bytesAvailable() > 0) {
				byte streamStatus = readSerialBytes()[0];

				if(streamStatus == (byte) STREAM_ACKNOWLEDGE_PACKET) {
					voxelDataStride += strideIncrease;
				}
				else if(streamStatus == (byte) STREAM_CORRUPTED_PACKET) {
					corruptedPackets++;
				}

				break;
			}
		}
	}
	private StreamData getCompressedStream() {
		byte[] stream = new byte[STREAM_SIZE];

		int strideIncrease = 0;
		for(int i = 0; i + 1 < STREAM_SIZE && voxelDataStride + strideIncrease < voxelData.length; i += 2) {
			stream[i + 1] = 1;
			for(int j = 0; voxelDataStride + strideIncrease + j < voxelData.length; j++) {
				if(j == 0)
					stream[i] = voxelData[voxelDataStride + strideIncrease];
				else if(voxelData[voxelDataStride + strideIncrease + j] == stream[i])
					stream[i + 1]++;
				else
					break;
			}
			strideIncrease += stream[i + 1];
		}

		return new StreamData(stream, strideIncrease);
	}
	private StreamData getRawStream() {
		byte[] stream = new byte[STREAM_SIZE];

		int strideIncrease = 0;
		for(int i = 0; i < STREAM_SIZE && voxelDataStride + strideIncrease < voxelData.length; i++) {
			stream[i] = voxelData[voxelDataStride + strideIncrease];
			strideIncrease++;
		}

		return new StreamData(stream, strideIncrease);
	}

	protected int getCubeSide() {
		return cubeSide;
	}
	protected int getChannelCount() {
		return channelCount;
	}
	protected int getChannelSize() {
		return channelSize;
	}

	protected int getFailedFrames() {
		return failedFrames;
	}

	private boolean setCubeParams(int cubeSide, int channelCount, int channelSize) {
		if(cubeSide < 0 || (channelCount < 0 || channelCount > 3) || (channelSize != 1 && channelSize != 2 && channelSize != 4 && channelSize != 8)) {
			return false;
		}

		this.cubeSide = cubeSide;
		this.channelCount = channelCount;
		this.channelSize = channelSize;

		voxelData = new byte[Frame.getVoxelDataSize(cubeSide, channelCount, channelSize)];
		return true;
	}
	private void sendSerialBytes(byte ... bytes) {
		if(serialPort == null)
			return;
		serialPort.writeBytes(bytes, bytes.length);
	}
	private byte[] readSerialBytes() {
		if(serialPort == null)
			return new byte[0];

		byte[] bytes = new byte[serialPort.bytesAvailable()];
		serialPort.readBytes(bytes, bytes.length);
		return bytes;
	}
	private void clearSerialBytes() {
		if(serialPort == null)
			return;

		byte[] trashBin = new byte[serialPort.bytesAvailable()];
		serialPort.readBytes(trashBin, trashBin.length);
	}
	private void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		}
		catch(InterruptedException e) {}
	}
}

class StreamData {
	private final byte[] data;
	private final int totalChannelStride;

	public StreamData(byte[] data, int totalChannelStride) {
		this.data = data;
		this.totalChannelStride = totalChannelStride;
	}
	public byte[] getData() {
		return data;
	}
	public int getTotalChannelStride() {
		return totalChannelStride;
	}
}
