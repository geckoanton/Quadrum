/*
 * Initializer.java
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

package main;

import java.io.*;
import java.nio.ByteBuffer;

public class Initializer {

	private static final String INIT_DATA_FILE_NAME = ".quadrum37";

	boolean windowMaximized = false;
	private float windowX = -1, windowY = -1,
			windowWidth = -1, windowHeight = -1;
	private boolean gridSetting = false;

	private String fileLocation = null;

	public boolean checkInitData() {
		fileLocation = System.getProperty("user.home") + "/" + INIT_DATA_FILE_NAME;
		File file = new File(fileLocation);
		if(file.exists()) {
			return readInitData(fileLocation);
		}
		return false;
	}
	public void writeInitData(boolean gridSetting,
							  boolean windowMaximized,
							  float windowX, float windowY,
							  float windowWidth, float windowHeight) {
		if(fileLocation == null)
			return;

		try {
			OutputStream outputStream = new FileOutputStream(fileLocation);

			outputStream.write((byte) (gridSetting ? 0xff : 0x0));
			outputStream.write((byte) (windowMaximized ? 0xff : 0x0));
			outputStream.write(ByteBuffer.allocate(4).putFloat(windowX).array());
			outputStream.write(ByteBuffer.allocate(4).putFloat(windowY).array());
			outputStream.write(ByteBuffer.allocate(4).putFloat(windowWidth).array());
			outputStream.write(ByteBuffer.allocate(4).putFloat(windowHeight).array());

			outputStream.close();
		}
		catch(IOException e) {}
	}

	public boolean isWindowMaximized() {
		return windowMaximized;
	}
	public float getWindowX() {
		return windowX;
	}
	public float getWindowY() {
		return windowY;
	}
	public float getWindowWidth() {
		return windowWidth;
	}
	public float getWindowHeight() {
		return windowHeight;
	}
	public boolean isGrid() {
		return gridSetting;
	}

	private boolean readInitData(String path) {
		try {
			InputStream inputStream = new FileInputStream(path);

			byte[] gridSettingBuffer = new byte[1];
			inputStream.read(gridSettingBuffer);
			gridSetting = (gridSettingBuffer[0] & 0xFF) == 255;

			byte[] fullScreenBuffer = new byte[1];
			inputStream.read(fullScreenBuffer);
			windowMaximized = (fullScreenBuffer[0] & 0xFF) == 255;

			byte[] windowXBuffer = new byte[4];
			inputStream.read(windowXBuffer);
			windowX = ByteBuffer.wrap(windowXBuffer).getFloat();

			byte[] windowYBuffer = new byte[4];
			inputStream.read(windowYBuffer);
			windowY = ByteBuffer.wrap(windowYBuffer).getFloat();

			byte[] windowWidthBuffer = new byte[4];
			inputStream.read(windowWidthBuffer);
			windowWidth = ByteBuffer.wrap(windowWidthBuffer).getFloat();

			byte[] windowHeightBuffer = new byte[4];
			inputStream.read(windowHeightBuffer);
			windowHeight = ByteBuffer.wrap(windowHeightBuffer).getFloat();
		}
		catch(IOException e) {
			return false;
		}
		return true;
	}
}
