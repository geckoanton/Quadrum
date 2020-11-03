/*
 * Frame.java
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

package project;

import main.GeneralMethods;
import java.util.Arrays;

public class Frame {
	public static final String DEFAULT_FRAME_NAME = "FRAME";
	private static final float STARTUP_FRAME_DURATION = 100.0f;

	private String name = DEFAULT_FRAME_NAME;
	private float durationInMillis = STARTUP_FRAME_DURATION;
	private int side;
	private Voxel[][][] voxels;

	public Frame(int side) {
		this.side = side;

		voxels = new Voxel[getSide()][getSide()][getSide()];
		for(int x = 0; x < getSide(); x++)
			for(int y = 0; y < getSide(); y++)
				for(int z = 0; z < getSide(); z++)
					voxels[x][y][z] = new Voxel();
	}
	public Frame(Frame copyFrame) {
		this.side = copyFrame.getSide();
		durationInMillis = copyFrame.getDurationInMillis();
		// does not copy frame name

		voxels = new Voxel[copyFrame.getSide()][copyFrame.getSide()][copyFrame.getSide()];
		for(int x = 0; x < getSide(); x++)
			for(int y = 0; y < getSide(); y++)
				for(int z = 0; z < getSide(); z++)
					voxels[x][y][z] = new Voxel(copyFrame.getVoxel(x, y, z));
	}

	public int getSide() {
		return side;
	}

	public Voxel getVoxel(int x, int y, int z) {
		if(x >= side || y >= side || z >= side)
			return null;
		return voxels[x][y][z];
	}

	public String getDisplayName(int displayIndex) {
		return name + " [" + (displayIndex + 1) + "]";	// used to set frameListView cell name
	}

	public synchronized float getDurationInMillis() {
		return durationInMillis;
	}
	public void setDurationInMillis(float durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		if(name.length() > Project.FRAME_NAME_LENGTH)	// when frame name gets stored in file by Project, it has a maximum name length
			this.name = name.substring(0, Project.FRAME_NAME_LENGTH);
	}

	public boolean equals(Frame frame) {
		// do not compare the frames' names
		if(getSide() != frame.getSide() || getDurationInMillis() != frame.getDurationInMillis())
			return false;
		for(int x = 0; x < getSide(); x++)
			for(int y = 0; y < getSide(); y++)
				for(int z = 0; z < getSide(); z++)
					if(!voxels[x][y][z].equals(frame.getVoxel(x, y, z)))
						return false;
		return true;
	}

	public void updateVoxelData(byte[] voxelData, int channelCount, int channelSize) {
		int channelMaxValue = (int) Math.pow(2, channelSize) - 1;

		Arrays.fill(voxelData, (byte) 0);

		for(int z = 0; z < side; z++) {
			for(int y = 0; y < side; y++) {
				for(int x = 0; x < side; x++) {
					if(channelCount == 1) {
						int bitOffset = channelSize * (((int) Math.pow(side, 2) * z) + (side * y) + x);
						int byteOffset = (int) Math.floor((float) (bitOffset) / (float) (GeneralMethods.BYTE_SIZE));
						int inByteOffset = bitOffset % GeneralMethods.BYTE_SIZE;

						float channelIntensity = (float) Math.max(getVoxel(x, y, side - 1 - z).getColor().getRed(), Math.max(getVoxel(x, y, side - 1 - z).getColor().getGreen(), getVoxel(x, y, side - 1 - z).getColor().getBlue()));
						int channelValue = Math.round(channelIntensity * channelMaxValue);

						int channelMask = channelValue << inByteOffset;
						voxelData[byteOffset] |= (byte) channelMask;
					}
					else if(channelCount > 1) {
						for(int i = 0; i < channelCount; i++) {
							int bitOffset = (channelSize * i) + channelCount * channelSize * (((int) Math.pow(side, 2) * z) + (side * y) + x);
							int byteOffset = (int) Math.floor((float) (bitOffset) / (float) (GeneralMethods.BYTE_SIZE));
							int inByteOffset = bitOffset % GeneralMethods.BYTE_SIZE;

							float channelIntensity = 0;

							switch(i) {
								case 0:
									channelIntensity = (float) getVoxel(x, y, side - 1 - z).getColor().getRed();
									break;
								case 1:
									channelIntensity = (float) getVoxel(x, y, side - 1 - z).getColor().getGreen();
									break;
								case 2:
									channelIntensity = (float) getVoxel(x, y, side - 1 - z).getColor().getBlue();
									break;
							}

							int channelValue = Math.round(channelIntensity * channelMaxValue);

							int channelMask = channelValue << inByteOffset;
							voxelData[byteOffset] |= (byte) channelMask;
						}
					}
				}
			}
		}
	}
	public static int getVoxelDataSize(int cubeSide, int channelCount, int channelSize) {
		int voxelBitSize = ((int) Math.pow(cubeSide, 3)) * channelSize * channelCount;
		return (int) Math.ceil(((float) voxelBitSize) / ((float) GeneralMethods.BYTE_SIZE));
	}
}
