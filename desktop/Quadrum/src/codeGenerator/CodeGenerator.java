/*
 * CodeGenerator.java
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

package codeGenerator;

import main.GeneralMethods;
import project.Frame;
import project.Project;
import java.nio.ByteBuffer;

public class CodeGenerator {
	public static String getCodeString(Project project, int channelCount, int channelSize) {
		StringBuilder generatedString = new StringBuilder();
		
		StringBuilder stringData = new StringBuilder();

		stringData.append("0x").append(GeneralMethods.byteToHex((byte) project.getSide()));
		stringData.append(",");
		stringData.append("0x").append(GeneralMethods.byteToHex((byte) channelCount));
		stringData.append(",");
		stringData.append("0x").append(GeneralMethods.byteToHex((byte) channelSize));
		byte[] frameCountBytes = ByteBuffer.allocate(2).putShort((short) project.getFrameSize()).array();
		for(int j = frameCountBytes.length - 1; j >= 0; j--) {
			stringData.append(",");
			stringData.append("0x").append(GeneralMethods.byteToHex(frameCountBytes[j]));
		}

		int totalArraySize = 5;	// size 5 because of init data above
		for(int i = 0; i < project.getFrameSize(); i++) {
			Frame frame = project.getFrame(i);
			byte[] frameVoxelData = new byte[Frame.getVoxelDataSize(project.getSide(), channelCount, channelSize)];

			totalArraySize += frameVoxelData.length + 4;
			frame.updateVoxelData(frameVoxelData, channelCount, channelSize);

			byte[] frameDurationBytes = ByteBuffer.allocate(4).putFloat(project.getFrame(i).getDurationInMillis()).array();
			for(int j = frameDurationBytes.length - 1; j >= 0; j--) {
				stringData.append(",");
				stringData.append("0x").append(GeneralMethods.byteToHex(frameDurationBytes[j]));
			}

			for(int j = 0; j < frameVoxelData.length; j++) {
				stringData.append(",");
				stringData.append("0x").append(GeneralMethods.byteToHex(frameVoxelData[j]));
			}
		}

		generatedString.append("const uint8_t animation[" + totalArraySize + "] = {" + stringData + "};");

		return generatedString.toString();
	}
}
