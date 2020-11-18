/*
 * Plane.java
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

package ui.editor.controls.paint;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import main.Editor;
import project.Project;
import project.Voxel;
import project.userActions.specialActions.stackActions.VoxelColorAction;
import ui.ButtonState;

public class Plane {
	protected static final int VOXEL_BUTTON_BORDER = 2, STRING_BORDER = 13;
	private static final Color PLANE_PREFIX_COLOR = Color.BLACK;
	private static final String PLANE_PREFIX = "Plane ";
	private static final Font TEXT_FONT = Font.loadFont(Editor.DEFAULT_FONT_LOCATION, 12);

	private Project project;
	private int yPlane;	// sets y position to modify in project frame voxels

	private float x = 0, y = 0, size = 0;

	public Plane(Project project, int yPlane) {
		this.project = project;
		this.yPlane = yPlane;
	}

	protected void setPosSize(float x, float y, float size) {
		this.x = x;
		this.y = y;
		this.size = size;

		float voxelButtonSize = (size - (VOXEL_BUTTON_BORDER * (project.getSide() - 1))) / project.getSide();
		for (int b = 0; b < project.getSide(); b++)
			for(int a = 0; a < project.getSide(); a++)
				project.getFrame().getVoxel(a, yPlane, b).setButtonPosSize(a * (voxelButtonSize + VOXEL_BUTTON_BORDER) + x,
						b * (voxelButtonSize + VOXEL_BUTTON_BORDER) + y,
						voxelButtonSize);
	}

	protected int mouseAction(PaintControls paintControls, int frameIndex, float mouseX, float mouseY, int previousVoxel) {
		if(intersects(mouseX, mouseY)) {
			for (int z = 0; z < project.getSide(); z++) {
				for (int x = 0; x < project.getSide(); x++) {
					if(project.getFrame().getVoxel(x, yPlane, z).intersects(mouseX, mouseY)) {
						// execute action on this particular voxel if it was not the previous one
						int currentVoxel = (x) + (yPlane * project.getSide()) + (z * (int) Math.pow(project.getSide(), 2));
						if(previousVoxel != currentVoxel)
							project.executeOnStack(new VoxelColorAction(paintControls, frameIndex, yPlane, project.getFrame().getVoxel(x, yPlane, z), project.getPaintColor()));
						return currentVoxel;
					}
				}
			}
		}
		return -1;
	}

	private boolean intersects(float mouseX, float mouseY) {
		return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
	}

	protected void draw(GraphicsContext gc, float mouseX, float mouseY, boolean mousePressedState) {
		for (int z = 0; z < project.getSide(); z++) {
			for (int x = 0; x < project.getSide(); x++) {
				Voxel voxel = project.getFrame().getVoxel(x, yPlane, z);
				voxel.drawButton(gc, getButtonState(voxel, mouseX, mouseY, mousePressedState));
			}
		}
		gc.setFill(PLANE_PREFIX_COLOR);
		gc.setFont(TEXT_FONT);
		gc.fillText(PLANE_PREFIX + (yPlane + 1), x, y + size + STRING_BORDER);
	}
	private ButtonState getButtonState(Voxel voxel, float mouseX, float mouseY, boolean mousePressedState) {
		ButtonState result = ButtonState.NEUTRAL;
		if(intersects(mouseX, mouseY)) {
			if(voxel.intersects(mouseX, mouseY)) {
				result = ButtonState.HOVERED;
				if(mousePressedState)
					result = ButtonState.PRESSED;
			}
		}
		return result;
	}
}
