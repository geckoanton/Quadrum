/*
 * Vec3.java
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

package matrixAlgebra;

public class Vec3 {
	private float[] vec3 = new float[3];

	public Vec3(float x, float y, float z) {
		set(0, x); set(1, y); set(2, z);
	}
	public Vec3(){
		for(int x = 0; x < 3; x++)	// clear vector
			vec3[x] = 0;
	}
	public float get(int x) {
		if(x < 0 || x >= 3)
			return 0;
		return vec3[x];
	}
	public int set(int x, float value) {
		if(x < 0 || x >= 3)
			return 1;
		vec3[x] = value;
		return 0;
	}
}
