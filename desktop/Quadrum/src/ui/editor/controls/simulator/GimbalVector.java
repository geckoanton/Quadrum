/*
 * GimbalVector.java
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

package ui.editor.controls.simulator;

import matrixAlgebra.Vec3;

public class GimbalVector {
	private Vec3 vector;
	private int initialIndex;

	public GimbalVector(Vec3 vector, int initialIndex) {
		this.vector = vector;
		this.initialIndex = initialIndex;
	}

	public Vec3 getVector() {
		return vector;
	}
	public int getInitialIndex() {
		return initialIndex;
	}
}
