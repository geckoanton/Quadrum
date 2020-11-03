/*
 * VoxelColorAction.java
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

package project.userActions.specialActions.stackActions;

import javafx.scene.paint.Color;
import project.Project;
import project.Voxel;
import project.userActions.StackAction;
import ui.editor.controls.paint.PaintControls;

public class VoxelColorAction implements StackAction {
	private PaintControls paintControls;
	private int frameIndex, plane;
	private Voxel voxel;
	private Color newColor,
			oldColor = null;

	public VoxelColorAction(PaintControls paintControls, int frameIndex, int plane, Voxel voxel, Color newColor) {
		this.paintControls = paintControls;
		this.frameIndex = frameIndex;
		this.plane = plane;

		this.voxel = voxel;
		this.newColor = newColor;
	}

	@Override
	public boolean execute(Project project) {
		if(voxel == null)
			return false;
		if(voxel.getColor().getRed() == newColor.getRed() &&
				voxel.getColor().getGreen() == newColor.getGreen() &&
				voxel.getColor().getBlue() == newColor.getBlue())
			return false;
		oldColor = voxel.getColor();
		voxel.setColor(newColor);
		paintControls.displayFramePlane(frameIndex, plane);
		return true;
	}
	@Override
	public void undo(Project project) {
		voxel.setColor(oldColor);
		paintControls.displayFramePlane(frameIndex, plane);
	}
}
