/*
 * NewProjectControls.java
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

package ui.newProjectPage;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import main.ProjectManager;
import project.Project;
import project.Voxel;
import ui.ButtonState;

public class NewProjectControls {
	private static final int MAX_SIDE = 8,
			MIN_SIDE = 2,
			START_SIDE = 4,
			BUTTON_BORDER = 2,
			EDGE_BORDER = 0;
	private static final Color SELECTED_BUTTON_COLOR = new Color(0.63f, 0.85f, 0.49f, 1.0f),
			NON_SELECTED_BUTTON_COLOR = new Color(0.82f, 0.82f, 0.82f, 1.0f);

	private int selectedSide;

	private boolean mousePressedState = false;
	private int hoverSide = -1;

	private Voxel[][] voxelButtons = new Voxel[MAX_SIDE][MAX_SIDE];

	private ProjectManager projectManager;
	private Canvas canvas;
	private TextField projectNameTextField;
	private Text sideText;

	public NewProjectControls(ProjectManager projectManager, Canvas canvas, TextField projectNameTextField, Text sideText) {
		this.projectManager = projectManager;
		this.canvas = canvas;
		this.projectNameTextField = projectNameTextField;
		this.sideText = sideText;

		for(int x = 0; x < MAX_SIDE; x++)
			for(int y = 0; y < MAX_SIDE; y++)
				voxelButtons[x][y] = new Voxel();

		setSelectedSide(START_SIDE);
		projectNameTextField.setPromptText(Project.DEFAULT_PROJECT_NAME);
	}

	public void mouseMoved(float mouseX, float mouseY) {
		mouseState(mouseX, mouseY, false);
	}
	public void mousePressed(float mouseX, float mouseY) {
		mouseState(mouseX, mouseY, true);
	}
	public void mouseDragged(float mouseX, float mouseY) {
		mouseState(mouseX, mouseY, true);
	}
	public void mouseReleased(float mouseX, float mouseY) {
		mouseAction(mouseX, mouseY, false);
	}

	private void mouseAction(float mouseX, float mouseY, boolean mousePressedState) {
		setSelectedSide(hoverSide);
		mouseState(mouseX, mouseY, mousePressedState);
	}
	private void mouseState(float mouseX, float mouseY, boolean mousePressedState) {
		this.mousePressedState = mousePressedState;
		for(int x = 0; x < MAX_SIDE; x++) {
			for (int y = 0; y < MAX_SIDE; y++) {
				if(voxelButtons[x][y].intersectsWithBorder(mouseX, mouseY, BUTTON_BORDER)) {
					setHoverSide(Math.max(x + 1, MAX_SIDE - y));
					draw();
					return;
				}
			}
		}
		setHoverSide(-1);
		draw();
	}

	public void updateLayout() {
		float limitingBreadth = Math.min((float) canvas.getWidth(), (float) canvas.getHeight());

		float size = limitingBreadth - (2 * EDGE_BORDER);

		float centerX = size / 2;
		float centerY = (float) canvas.getHeight() / 2;

		float leftX = centerX - (size / 2);
		float topY = centerY - (size / 2);

		float buttonSize = (size - ((MAX_SIDE - 1) * BUTTON_BORDER)) / MAX_SIDE;
		float shiftSize = buttonSize + BUTTON_BORDER;

		for(int x = 0; x < MAX_SIDE; x++) {
			for(int y = 0; y < MAX_SIDE; y++) {
				float buttonX = leftX + (shiftSize * x);
				float buttonY = topY + (shiftSize * y);
				voxelButtons[x][y].setButtonPosSize(buttonX, buttonY, buttonSize);

				if(x < selectedSide && (MAX_SIDE - y - 1) < selectedSide)
					voxelButtons[x][y].setColor(SELECTED_BUTTON_COLOR);
				else
					voxelButtons[x][y].setColor(NON_SELECTED_BUTTON_COLOR);
			}
		}

		draw();
	}
	public void draw() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		for(int x = 0; x < MAX_SIDE; x++) {
			for (int y = 0; y < MAX_SIDE; y++) {
				ButtonState buttonState = ButtonState.NEUTRAL;
				if(x < hoverSide && (MAX_SIDE - y - 1) < hoverSide) {
					if(mousePressedState)
						buttonState = ButtonState.PRESSED;
					else
						buttonState = ButtonState.HOVERED;
				}
				voxelButtons[x][y].drawButton(gc, buttonState);
			}
		}
	}

	private void setHoverSide(int hoverSide) {
		if(hoverSide >= 0) {
			if (hoverSide < MIN_SIDE)
				hoverSide = MIN_SIDE;
			else if (hoverSide > MAX_SIDE)
				hoverSide = MAX_SIDE;
		}
		this.hoverSide = hoverSide;
	}

	public void setSelectedSide(int selectedSide) {
		if(selectedSide < MIN_SIDE || selectedSide > MAX_SIDE)
			return;
		this.selectedSide = selectedSide;
		sideText.setText(Integer.toString(selectedSide));
		updateLayout();
	}

	public void decrementSelectedSide() {
		setSelectedSide(selectedSide - 1);
	}
	public void incrementSelectedSide() {
		setSelectedSide(selectedSide + 1);
	}

	public void createProject() {
		projectManager.addNew(projectNameTextField.getText(), selectedSide);
	}
}
