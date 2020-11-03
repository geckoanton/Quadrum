/*
 * Mat3.java
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

package matrixAlgebra;

public class Mat3 {
	private float[][] mat3 = new float[3][3];

	public Mat3(float x1, float y2, float z3) {
		clearMatrix();
		set(0, 0, x1); set(1, 1, y2); set(2, 2, z3);
	}
	public Mat3() {
		setUnitMatrix();
	}
	public float get(int x, int y) {
		if(x < 0 || x >= 3 || y < 0 || y >= 3)
			return 0;
		return mat3[x][y];
	}
	public int set(int x, int y, float value) {
		if(x < 0 || x >= 3 || y < 0 || y >= 3)
			return 1;
		mat3[x][y] = value;
		return 0;
	}
	public Vec3 mult(Vec3 vector) {
		Vec3 resultVec = new Vec3(0, 0, 0);
		for(int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				resultVec.set(y, resultVec.get(y) + (mat3[x][y] * vector.get(x)));

		return resultVec;
	}
	public Mat3 mult(Mat3 matrix) {
		Mat3 resultMat = new Mat3(0, 0, 0);
		for(int x = 0; x < 3; x++)
			for(int y = 0; y < 3; y++)
				for(int i = 0; i < 3; i++)
					resultMat.set(x, y, resultMat.get(x, y) + (get(i, y) * matrix.get(x, i)));

		return resultMat;
	}
	public void setUnitMatrix() {
		clearMatrix();
		mat3[0][0] = 1; mat3[1][1] = 1; mat3[2][2] = 1;
	}
	private void clearMatrix() {
		for(int x = 0; x < 3; x++)
			for(int y = 0; y < 3; y++)
				mat3[x][y] = 0;
	}

	public static Mat3 rotateHorizontal(float angle) {
		Mat3 resultMat = new Mat3();
		resultMat.set(0, 0, (float) Math.cos(angle));
		resultMat.set(2, 0, (float) Math.sin(angle));
		resultMat.set(0, 2, (float) -Math.sin(angle));
		resultMat.set(2, 2, (float) Math.cos(angle));
		return resultMat;
	}
	public static Mat3 rotateVertical(float angle) {
		Mat3 resultMat = new Mat3();
		resultMat.set(1, 1, (float) Math.cos(angle));
		resultMat.set(2, 1, (float) -Math.sin(angle));
		resultMat.set(1, 2, (float) Math.sin(angle));
		resultMat.set(2, 2, (float) Math.cos(angle));
		return resultMat;
	}
}
