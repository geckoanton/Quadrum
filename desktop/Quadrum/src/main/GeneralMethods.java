/*
 * GeneralMethods.java
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

package main;

import java.awt.*;
import java.net.URI;

public class GeneralMethods {
	public static final int BYTE_SIZE = 8;

	public static byte[] stringToByteArray(String stringName, int byteLength) {
		byte[] retArr = new byte[byteLength];
		for(int i = 0; i < byteLength; i++)
			retArr[i] = 0;
		if(stringName == null)
			return retArr;
		for(int i = 0; i < stringName.length(); i++)
			retArr[i] = (byte) stringName.charAt(i);
		return retArr;
	}
	public static String byteArrayToString(byte[] array) {
		StringBuilder result = new StringBuilder(array.length);
		for(int i = 0; i < array.length; i++) {
			if(array[i] == 0)
				break;
			result.append((char) array[i]);
		}
		return result.toString();
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String byteToHex(byte data) {
		char[] hexChars = new char[2];
		int v = data & 0xFF;
		hexChars[0] = HEX_ARRAY[v >>> 4];
		hexChars[1] = HEX_ARRAY[v & 0x0F];
		return new String(hexChars);
	}

	public static void openUrl(String urlString) {
		try {
			String osString = System.getProperty("os.name").toLowerCase();
			if (osString.contains("nix") || osString.contains("nux") || osString.contains("aix")) {
				// workaround for linux because "Desktop.getDesktop().browse()" doesn't work on some linux implementations
				if (Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1) {
					Runtime.getRuntime().exec(new String[] { "xdg-open", urlString });
				}
			}
			else {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(urlString));
				}
			}
		}
		catch (Exception e) {}
	}
}
