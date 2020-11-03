/*
 * QuadrumSerial.cpp
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

#include "Arduino.h"
#include "QuadrumSerial.h"
#include <math.h>

QuadrumSerial::QuadrumSerial(uint16_t cubeSide, uint16_t channelCount, uint16_t channelSize, uint16_t planeRate) {
    startup(cubeSide, channelCount, channelSize, planeRate);
}
void QuadrumSerial::startup(uint16_t cubeSide, uint16_t channelCount, uint16_t channelSize, uint16_t planeRate) {
	if(!(channelSize == 1 || channelSize == 2 || channelSize == 4 || channelSize == 8))
		channelSize = 1;

    this->cubeSide = min((int16_t)cubeSide, MAX_CUBE_SIDE);
    this->channelCount = min((int16_t)channelCount, MAX_CHANNEL_COUNT);
	this->channelSize = channelSize;
	this->channelMaxValue = (int16_t) (pow(2, channelSize) - 1);

    this->dataRetrieveWaitTime = (1000000.0f / (float) planeRate) - DATA_RETREIVE_WAIT_TIME_MARGIN;

    int16_t voxelCount = cubeSide * cubeSide * cubeSide;
    uint32_t voxelBitSize = voxelCount * channelSize * channelCount;
	this->voxelDataStride = 0;
    this->voxelDataSize = ceil((float)voxelBitSize / (float)BYTE_SIZE);

    this->writeVoxelData = new int8_t[voxelDataSize];
	this->displayVoxelData = new int8_t[voxelDataSize];
	
	for(int8_t i = 0; i < voxelDataSize; i++) {
		writeVoxelData[i] = 0;
		displayVoxelData[i] = 0;
	}
}

void QuadrumSerial::start() {
	initSerialWait();

	while(continueCommunication) {
		uint32_t startTime = micros();
		uint32_t midRequestTime = startTime;
		
		while(requestData(startTime, midRequestTime));
		
		float delayTime = (float) ((dataRetrieveWaitTime) + DATA_RETREIVE_WAIT_TIME_MARGIN) - (float) (micros() - midRequestTime);
		delayMicroseconds(max((int32_t) delayTime, 0));
		
		loop();
	}

	while(true) {	// make sure anything after start call never gets executed
		loop();
		if(serialEventRun) serialEventRun();
	}
}
void QuadrumSerial::terminate() {
	sendChar(TERMINATE_COMMUNICATION_PACKET);
	Serial.end();
	continueCommunication = false;
}

void QuadrumSerial::initSerialWait() {
	Serial.begin(SERIAL_SPEED);

	int8_t initData = 0;
	while(true) {
		uint32_t midRequestTime = 0;
		if(retrieveBuffer(micros(), midRequestTime, &initData, 1)) {	// retrieve buffer with current time so that it will load it as long as possible
			if((uint8_t) initData == INIT_REQUEST_PACKET) {
				sendChar(INIT_START_PACKET);
				sendChar((int8_t) cubeSide);
				sendChar((int8_t) channelCount);
				sendChar((int8_t) channelSize);
				
				int8_t initAcknowledge = 0;
				retrieveBuffer(micros(), midRequestTime, &initAcknowledge, 1);	// send millis() as startTime to get maximum response time
				if((uint8_t) initAcknowledge == INIT_ACKNOWLEDGE_PACKET)
					break;
			}
		}
	}
}

bool QuadrumSerial::requestData(uint32_t startTime, uint32_t& midRequestTime) {
	if(!clearSerialBuffer(startTime, midRequestTime))
		return false;

	sendChar(STREAM_REQUEST_PACKET);

	{
		// streamStartData - {streamFormat}/display current voxelData
		int8_t streamStartData;
		if(!retrieveBuffer(startTime, midRequestTime, &streamStartData, 1))
			return false;

		if((uint8_t) streamStartData == STREAM_START_COMPRESSED_PACKET) {
			int8_t compressedStream[STREAM_SIZE];
			if(!retrieveBuffer(startTime, midRequestTime, compressedStream, STREAM_SIZE))
				return false;

			readCompressedStream(compressedStream, STREAM_SIZE);

			if(!endOfStream(startTime, midRequestTime))
				return false;
		}
		if((uint8_t) streamStartData == STREAM_START_RAW_PACKET) {
			int8_t rawStream[STREAM_SIZE];
			if(!retrieveBuffer(startTime, midRequestTime, rawStream, STREAM_SIZE))
				return false;

			readRawStream(rawStream, STREAM_SIZE);

			if(!endOfStream(startTime, midRequestTime))
				return false;
		}
		else if((uint8_t) streamStartData == DISPLAY_PACKET) {
			if(voxelDataStride == voxelDataSize) {
				swapDisplayBuffer();
				sendChar(FRAME_DISPLAYED_PACKET);
			}
			else {
				sendChar(FRAME_FAILED_PACKET);
			}
			voxelDataStride = 0;
		}
	}
}
bool QuadrumSerial::endOfStream(uint32_t startTime, uint32_t& midRequestTime) {
	int8_t endStreamData[2];
	if(!retrieveBuffer(startTime, midRequestTime, endStreamData, 2))
		return false;
	
	// endStreamData[0] - end of stream packet, endStreamData[1] - checksum
	{
		if((uint8_t) endStreamData[0] == STREAM_END_PACKET) {
			// FUTURE: control checksum, if pass send ack otherwise corrupt
			sendChar(STREAM_ACKNOWLEDGE_PACKET);
			if(voxelDataStride + increasedStride <= voxelDataSize)
				voxelDataStride += increasedStride;
		}
		else {
			sendChar(STREAM_CORRUPTED_PACKET);
		}
	}
	return true;
}
void QuadrumSerial::readRawStream(int8_t* stream, uint16_t streamSize) {
	increasedStride = 0;
	for(int16_t i = 0; i < streamSize && voxelDataStride + i < voxelDataSize; i++) {
		writeVoxelData[voxelDataStride + i] = stream[i];
		increasedStride++;
	}
}
void QuadrumSerial::readCompressedStream(int8_t* stream, uint16_t streamSize) {
	increasedStride = 0;
	for(int16_t i = 0; i + 1 < streamSize && voxelDataStride + increasedStride < voxelDataSize; i += 2) {
		// stream[i + 0] - one byte in voxelData, stream[i + 1] - number of bytes to apply stream[i + 0] to in voxelData
		for(int16_t j = 0; j < (int16_t) stream[i + 1] && voxelDataStride + increasedStride < voxelDataSize; j++) {
			writeVoxelData[voxelDataStride + increasedStride] = stream[i + 0];
			increasedStride++;
		}
	}
}

void QuadrumSerial::swapDisplayBuffer() {
	int8_t* t = displayVoxelData;
	displayVoxelData = writeVoxelData;
	writeVoxelData = t;
}
bool QuadrumSerial::clearSerialBuffer(uint32_t startTime, uint32_t& midRequestTime) {
	while(Serial.available() > 0 && hasTimeLeft(startTime, midRequestTime))
		Serial.read();
	if(Serial.available() > 0)
		return false;
	return true;
}
bool QuadrumSerial::retrieveBuffer(uint32_t startTime, uint32_t& midRequestTime, int8_t* buffer, uint16_t bufferSize) {
	uint16_t i = 0;
	while(i < bufferSize) {
		if(Serial.available() > 0) {
			buffer[i] = Serial.read();
			i++;
		}
		if(!hasTimeLeft(startTime, midRequestTime))
			return false;
	}
	return true;
}
void QuadrumSerial::sendChar(int8_t data) {
	Serial.write(data);
}
bool QuadrumSerial::hasTimeLeft(uint32_t startTime, uint32_t& midRequestTime) {
	if(midRequestTime == 0) {
		return true;
	}
	else if(((float) micros() - (float) startTime) < MAX_SERIAL_RETREIVE_WAIT) {
		if((float) (micros() - midRequestTime) > (dataRetrieveWaitTime)) {
			float delayTime = (float) ((dataRetrieveWaitTime) + DATA_RETREIVE_WAIT_TIME_MARGIN) - (float) (micros() - midRequestTime);
			delayMicroseconds(max((int32_t) delayTime, 0));
			
			loop();
			
			midRequestTime = micros();
		}
		return true;
	}
	return false;
}

uint8_t QuadrumSerial::getChannelBrightness(uint16_t channel, uint16_t x, uint16_t y, uint16_t z) {
    if(channel >= channelCount)
		return 0;
	
	int16_t voxelPos = ((cubeSide * cubeSide) * z) + (cubeSide * y) + x;
	int16_t readPos = (voxelPos * channelCount * channelSize) + (channel * channelSize);
	
	int16_t bytePos = readPos / BYTE_SIZE;
	int16_t byteOffset = readPos % BYTE_SIZE;
	
	uint8_t mask = ((int8_t) channelMaxValue) << byteOffset;
	uint8_t channelBits = (uint8_t) ((displayVoxelData[bytePos] & mask) >> byteOffset);
	
	return (uint8_t) (((uint16_t) channelBits * 255) / (uint16_t) channelMaxValue);
}
uint8_t QuadrumSerial::getVoxelBrightness(uint16_t x, uint16_t y, uint16_t z) {
    if(channelCount != 1)
		return 0;
	return getChannelBrightness(0, x, y, z);
}
bool QuadrumSerial::getChannelState(uint16_t channel, uint16_t x, uint16_t y, uint16_t z) {
    if(channelSize != 1 || channel >= channelCount)
		return false;
	
	int16_t voxelPos = ((cubeSide * cubeSide) * z) + (cubeSide * y) + x;
	int16_t readPos = (voxelPos * channelCount) + channel;
	
	int16_t bytePos = readPos / BYTE_SIZE;
	int16_t byteOffset = readPos % BYTE_SIZE;
	
	int8_t mask = 0x1 << byteOffset;
	return (uint8_t) (displayVoxelData[bytePos] & mask) > 0;
}
bool QuadrumSerial::getVoxelState(uint16_t x, uint16_t y, uint16_t z) {
    if(channelCount != 1)
		return false;
	return getChannelState(0, x, y, z);
}
