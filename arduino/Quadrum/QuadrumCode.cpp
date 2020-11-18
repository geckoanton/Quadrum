/*
 * QuadrumCode.cpp
 *
 *       Created on:  Okt 29, 2020
 *  Last Updated on:  Nov 5, 2020
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

#include "Arduino.h"
#include "QuadrumCode.h"

QuadrumCode::QuadrumCode(const uint8_t* animationData, uint16_t planeRate) {
	startup(animationData, planeRate);
}
void QuadrumCode::startup(const uint8_t* animationData, uint16_t planeRate) {
	this->cubeSide = animationData[0];
	this->animationData = (int8_t*) animationData;
	this->loopCallWait = (1000000.0f / (float) planeRate);
	
	this->channelCount = animationData[1];
	this->channelSize = animationData[2];
	this->channelMaxValue = (int16_t) pow(2, channelSize) - 1;
	
	ByteToInt bti;
	bti.bytes[0] = (uint8_t) animationData[3];
	bti.bytes[1] = (uint8_t) animationData[4];
	this->frameCount = bti.value;
	
	int16_t frameBitSize = pow(cubeSide, 3) * channelCount * channelSize;
	this->frameDataSize = (int16_t) (ceil((float) frameBitSize / (float) BYTE_SIZE)) + 4;	// + 4 (sizeof(float)) because of frame duration at beginning
}

bool QuadrumCode::start() {
	if(cubeSide < 0)
		return false;
	
	while(continueAnimation) {
		uint32_t startTime = micros();
		
		if(micros() > incrementFrameTime) {
			ByteToFloat frameDuration;
			for(int16_t i = 0; i < sizeof(float); i++)
				frameDuration.bytes[i] = (uint8_t) animationData[INIT_DATA_SIZE + (currentFrame * frameDataSize) + i];
			incrementFrameTime = (uint32_t) (frameDuration.value * 1000.0f) + micros();
			
			voxelData = &animationData[INIT_DATA_SIZE + (currentFrame * frameDataSize) + sizeof(float)];
			currentFrame++;
			if(currentFrame >= frameCount)
				currentFrame = 0;
		}
		
		float delayTime = (float) loopCallWait - (float) (micros() - startTime);
		delayMicroseconds(max((int32_t)delayTime, 0));
		
		loop();
	}
	
	while(true) {	// make sure anything after start call never gets executed
		loop();
		if(serialEventRun) serialEventRun();
	}
}
void QuadrumCode::terminate() {
	continueAnimation = false;
}

uint8_t QuadrumCode::getChannelBrightness(uint16_t channel, uint16_t x, uint16_t y, uint16_t z) {
    if(channel >= channelCount)
		return 0;
	
	int16_t voxelPos = ((cubeSide * cubeSide) * z) + (cubeSide * y) + x;
	int16_t readPos = (voxelPos * channelCount * channelSize) + (channel * channelSize);
	
	int16_t bytePos = readPos / BYTE_SIZE;
	int16_t byteOffset = readPos % BYTE_SIZE;
	
	uint8_t mask = ((int8_t) channelMaxValue) << byteOffset;
	uint8_t channelBits = (uint8_t) ((voxelData[bytePos] & mask) >> byteOffset);
	
	return (uint8_t) (((uint16_t) channelBits * 255) / (uint16_t) channelMaxValue);
}
uint8_t QuadrumCode::getVoxelBrightness(uint16_t x, uint16_t y, uint16_t z) {
    if(channelCount != 1)
		return 0;
	return getChannelBrightness(0, x, y, z);
}
bool QuadrumCode::getChannelState(uint16_t channel, uint16_t x, uint16_t y, uint16_t z) {
    if(channelSize != 1 || channel >= channelCount)
		return false;
	
	int16_t voxelPos = ((cubeSide * cubeSide) * z) + (cubeSide * y) + x;
	int16_t readPos = (voxelPos * channelCount) + channel;
	
	int16_t bytePos = readPos / BYTE_SIZE;
	int16_t byteOffset = readPos % BYTE_SIZE;
	
	int8_t mask = 0x1 << byteOffset;
	return (uint8_t) (voxelData[bytePos] & mask) > 0;
}
bool QuadrumCode::getVoxelState(uint16_t x, uint16_t y, uint16_t z) {
    if(channelCount != 1)
		return false;
	return getChannelState(0, x, y, z);
}
