/*
 * Voxel.java
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

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import matrixAlgebra.Vec3;
import ui.ButtonState;

public class Voxel {
	public static final int MIN_VOXEL_SIZE = 8;
	private static final Color INVISIBLE_COLOR = Color.BLACK, STARTUP_VOXEL_COLOR = INVISIBLE_COLOR;

	private Color color = STARTUP_VOXEL_COLOR;
	private static float voxelSize = MIN_VOXEL_SIZE;

	public Voxel() {}
	public Voxel(Voxel copyVoxel) {
		this.color = copyVoxel.getColor();
	}

	public Color getColor() {
		return color;
	}
	public void setColor(Color newColor) {
		// ensure the voxel opacity never actually becomes transparent
		float red = (float) ((newColor.getRed() * newColor.getOpacity()) + (color.getRed() * (1 - newColor.getOpacity())));
		float green = (float) ((newColor.getGreen() * newColor.getOpacity()) + (color.getGreen() * (1 - newColor.getOpacity())));
		float blue = (float) ((newColor.getBlue() * newColor.getOpacity()) + (color.getBlue() * (1 - newColor.getOpacity())));
		color = new Color(red, green, blue, 1.0f);
	}

	public static void setVoxelSize(float s) {
		voxelSize = s;
	}

	public boolean equals(Voxel compareVoxel) { return color.equals(compareVoxel.getColor()); }

	public void draw(GraphicsContext gc, float centerX, float centerY, Vec3 position) {
		if(color.equals(INVISIBLE_COLOR))
			return;
		gc.setFill(color);
		gc.fillOval(centerX + position.get(0) - (voxelSize / 2), centerY - position.get(1) - (voxelSize / 2), voxelSize, voxelSize);
	}

	// voxel's corresponding button
	public static final int MIN_VOXEL_BUTTON_SIZE = 15;

	private static final Color HOVERED_COLOR = new Color(1, 1, 1, 0.3),
			PRESSED_COLOR = new Color(0, 0, 0, 0.3);

	private float buttonX = 0, buttonY = 0, buttonSize = 0;

	public boolean intersects(float mouseX, float mouseY) {
		return mouseX >= buttonX && mouseX < buttonX + buttonSize && mouseY >= buttonY && mouseY < buttonY + buttonSize;
	}
	public boolean intersectsWithBorder(float mouseX, float mouseY, int border) {
		return mouseX >= buttonX && mouseX < buttonX + buttonSize + border && mouseY >= buttonY && mouseY < buttonY + buttonSize + border;
	}

	public void setButtonPosSize(float buttonX, float buttonY, float buttonSize) {
		this.buttonX = buttonX;
		this.buttonY = buttonY;
		this.buttonSize = buttonSize;
	}

	public void drawButton(GraphicsContext gc, ButtonState buttonState) {
		gc.setFill(color);
		gc.fillRect(buttonX, buttonY, buttonSize, buttonSize);
		switch(buttonState) {
			case HOVERED:
				gc.setFill(HOVERED_COLOR);
				gc.fillRect(buttonX, buttonY, buttonSize, buttonSize);
				break;
			case PRESSED:
				gc.setFill(PRESSED_COLOR);
				gc.fillRect(buttonX, buttonY, buttonSize, buttonSize);
				break;
		}
	}
}
