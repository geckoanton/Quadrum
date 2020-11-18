/*
 * QuadrumCode.h
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

#ifndef QuadrumCode_h
#define QuadrumCode_h

class QuadrumCode {
	
public:

	QuadrumCode(const uint8_t* animationData, uint16_t planeRate);
	
	bool start();
	void terminate();

	// used when resolution of every channel is greater than 1 (can be between 0 and 1)
	uint8_t getChannelBrightness(uint16_t channel, uint16_t x, uint16_t y, uint16_t z);
	uint8_t getVoxelBrightness(uint16_t x, uint16_t y, uint16_t z); // used only with single channel
	// used when resolution of every channel is 1 (can only 0 or 1)
	bool getChannelState(uint16_t channel, uint16_t x, uint16_t y, uint16_t z);
	bool getVoxelState(uint16_t x, uint16_t y, uint16_t z); // used only with single channel
	
private:

	union ByteToInt {
		uint8_t bytes[2];
		int16_t value;
	};
	union ByteToFloat {
		uint8_t bytes[4];
		float value;
	};
	
	const int16_t BYTE_SIZE = 8,
			INIT_DATA_SIZE = 5;
	
	int16_t cubeSide, channelCount, channelSize, channelMaxValue;
	
	int16_t frameCount, frameDataSize;
	int16_t currentFrame = 0;
	uint32_t incrementFrameTime = 0;
	
	float loopCallWait;
	
	int8_t* animationData;
	int8_t* voxelData;
	
	bool continueAnimation = true;
	
	void startup(const uint8_t* animationData, uint16_t planeRate);
	
};

#endif
