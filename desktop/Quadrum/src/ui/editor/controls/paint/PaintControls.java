/*
 * PaintControls.java
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

package ui.editor.controls.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import main.ProjectManager;
import ui.editor.controls.ProjectControls;

public class PaintControls implements ProjectControls {
	private Canvas canvas;
	private PaintPanel paintPanel = null;
	private ProjectManager projectManager;

	private Button previousPageButton, nextPageButton;
	private ColorPicker paintColorPicker;

	public PaintControls(ProjectManager projectManager, Canvas canvas,
						 Button previousPageButton, Button nextPageButton, ColorPicker paintColorPicker) {
		this.projectManager = projectManager;
		this.canvas = canvas;

		this.previousPageButton = previousPageButton;
		this.nextPageButton = nextPageButton;
		this.paintColorPicker = paintColorPicker;
	}
	@Override
	public void updateProject() {
		paintPanel = new PaintPanel(projectManager.getCurrent(), canvas);
		if(projectManager.getCurrent() != null)
			paintColorPicker.setValue(projectManager.getCurrent().getPaintColor());
		updateLayout();
	}
	@Override
	public void updateLayout() {
		paintColorPicker.setDisable(projectManager.getCurrent() == null);

		if(paintPanel == null)
			return;
		paintPanel.updateLayout();
		nextPageButton.setDisable(paintPanel.isDisableNext());
		previousPageButton.setDisable(paintPanel.isDisablePrevious());
	}

	public void mouseMoved(float mouseX, float mouseY) {
		if (paintPanel == null)
			return;
		paintPanel.mouseState(mouseX, mouseY, false);
	}
	public void mousePressed(float mouseX, float mouseY) {
		if (paintPanel == null)
			return;
		paintPanel.mouseAction(this, mouseX, mouseY, true);
	}
	public void mouseDragged(float mouseX, float mouseY) {
		if (paintPanel == null)
			return;
		paintPanel.mouseAction(this, mouseX, mouseY, true);
	}
	public void mouseReleased(float mouseX, float mouseY) {
		if (paintPanel == null)
			return;
		paintPanel.mouseState(mouseX, mouseY, false);
	}

	public void nextPage() {
		if(paintPanel == null)
			return;
		paintPanel.nextPage();
		updateLayout();
	}
	public void previousPage() {
		if(paintPanel == null)
			return;
		paintPanel.previousPage();
		updateLayout();
	}
	public void updatePaintColor() {
		projectManager.getCurrent().setPaintColor(paintColorPicker.getValue());
	}
	public void displayFramePlane(int frameIndex, int plane) {
		projectManager.getCurrent().setCurrentFrame(frameIndex);
		paintPanel.displayPlane(plane);
		updateLayout();
	}
}
