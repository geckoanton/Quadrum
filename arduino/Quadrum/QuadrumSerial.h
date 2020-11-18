/*
 * QuadrumSerial.h
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

#ifndef QuadrumSerial_h
#define QuadrumSerial_h

class QuadrumSerial {

public:

	QuadrumSerial(uint16_t cubeSide, uint16_t channelCount, uint16_t channelSize, uint16_t planeRate);

	void start();
	void terminate();

	// used when resolution of every channel is greater than 1 (can be between 0 and 1)
	uint8_t getChannelBrightness(uint16_t channel, uint16_t x, uint16_t y, uint16_t z);
	uint8_t getVoxelBrightness(uint16_t x, uint16_t y, uint16_t z); // used only with single channel
	// used when resolution of every channel is 1 (can only 0 or 1)
	bool getChannelState(uint16_t channel, uint16_t x, uint16_t y, uint16_t z);
	bool getVoxelState(uint16_t x, uint16_t y, uint16_t z); // used only with single channel

private:

	const int16_t BYTE_SIZE = 8;
	const int16_t MAX_CUBE_SIDE = 8;
	const int16_t MAX_CHANNEL_COUNT = 3;
	const float MAX_INIT_RETREIVE_WAIT = 500000, MAX_SERIAL_RETREIVE_WAIT = 10000;
	const int32_t SERIAL_SPEED = 115200;
	const float DATA_RETREIVE_WAIT_TIME_MARGIN = 1000;
	const int16_t STREAM_SIZE = 8;
	const int16_t AVERAGE_LOOP_TIME_ITERATIONS = 256;

	int8_t *writeVoxelData;	// 2 buffers because of double buffering
	int16_t voxelDataStride, increasedStride;
	int8_t *displayVoxelData;
	int16_t voxelDataSize;

	int16_t cubeSide, channelCount, channelSize;
	float dataRetrieveWaitTime;

	int16_t channelMaxValue;

	bool continueCommunication = true;

	void startup(uint16_t cubeSide, uint16_t channelCount, uint16_t channelSize, uint16_t planeRate); // called from constructors

	void initSerialWait(); // runs only in the beginning of communication to establish correct paremeters (cubeSide, channelCount, channelSize) on PC

	bool requestData(uint32_t startTime, uint32_t& midRequestTime);	// returns hasTimeLeft
	bool endOfStream(uint32_t startTime, uint32_t& midRequestTime);	// returns true of end of stream was successful
	void readRawStream(int8_t* stream, uint16_t streamSize);
	void readCompressedStream(int8_t* stream, uint16_t streamSize);

	void swapDisplayBuffer();
	bool clearSerialBuffer(uint32_t startTime, uint32_t& midRequestTime);	// returns true if buffer was successfully cleared
	bool retrieveBuffer(uint32_t startTime, uint32_t& midRequestTime, int8_t* buffer, uint16_t bufferSize);	// returns true if buffer was successfully filled
	void sendChar(int8_t data);
	bool hasTimeLeft(uint32_t startTime, uint32_t& midRequestTime); // returns true if there is no time (dataRetrieveMax) left

	// protocol packet constants
	const int16_t INIT_REQUEST_PACKET = 127;
	const int8_t INIT_START_PACKET = 120;
	const int16_t INIT_ACKNOWLEDGE_PACKET = 113;

	const int8_t STREAM_REQUEST_PACKET = 63, TERMINATE_COMMUNICATION_PACKET = 0;
	const int16_t STREAM_START_COMPRESSED_PACKET = 32, STREAM_START_RAW_PACKET = 31, DISPLAY_PACKET = 24;
	const int16_t STREAM_END_PACKET = 248;
	const int8_t STREAM_ACKNOWLEDGE_PACKET = 240, STREAM_CORRUPTED_PACKET = 224;
	const int8_t FRAME_DISPLAYED_PACKET = 217, FRAME_FAILED_PACKET = 210;

};

#endif
