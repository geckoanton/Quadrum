/*
 * SimulatorControls.java
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

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import main.ProjectManager;
import project.Frame;
import serial.SerialInterface;
import ui.editor.EditorController;
import ui.editor.controls.ProjectControls;

public class SimulatorControls implements ProjectControls {
	private Canvas canvas;
	private SimulatorPanel simulatorPanel = null;
	private ProjectManager projectManager;

	private Button playAnimationButton, stopAnimationButton;
	private Text frameNameText;
	private MenuItem playMenuItem, stopMenuItem;

	private int frameOverrideIndex = -1;

	public SimulatorControls(ProjectManager projectManager, Canvas canvas,
							 Button playAnimationButton, Button stopAnimationButton, Text frameNameText,
							 MenuItem playMenuItem, MenuItem stopMenuItem) {
		this.projectManager = projectManager;
		this.canvas = canvas;

		this.playAnimationButton = playAnimationButton;
		this.stopAnimationButton = stopAnimationButton;
		this.frameNameText = frameNameText;

		this.playMenuItem = playMenuItem;
		this.stopMenuItem = stopMenuItem;
	}
	@Override
	public void updateProject() {
		simulatorPanel = new SimulatorPanel(projectManager.getCurrent(), canvas);
		updateLayout();
	}
	@Override
	public synchronized void updateLayout() {
		if(simulatorPanel == null)
			return;
		simulatorPanel.updateLayout(getDisplayFrame());

		if(projectManager.getCurrent() == null)
			setAnimationMode(true, true);
		else
			setAnimationMode(frameOverrideIndex >= 0, !(frameOverrideIndex >= 0));

		updateFrameNameText();

		float opacity = 1.0f;
		if(projectManager.getCurrent() == null)
			opacity = EditorController.DISABLED_OPACITY;
		frameNameText.setOpacity(opacity);
	}
	private void updateFrameNameText() {
		if(projectManager.getCurrent() == null) {
			frameNameText.setText(Frame.DEFAULT_FRAME_NAME);
		}
		else {
			int index = projectManager.getCurrent().getFrameIndex();
			if(frameOverrideIndex >= 0)
				index = frameOverrideIndex;

			frameNameText.setText(getDisplayFrame().getDisplayName(index));
		}
	}

	public void mousePressed(float mouseX, float mouseY) {
		if(simulatorPanel == null)
			return;
		simulatorPanel.setMouseOutsets(mouseX, mouseY);
	}
	public void mouseDragged(float mouseX, float mouseY) {
		if(simulatorPanel == null)
			return;
		simulatorPanel.setMouseRelative(mouseX, mouseY, getDisplayFrame());
	}

	public void playAnimation() {
		if(projectManager.getCurrent() == null)
			return;
		setAnimationMode(true, false);
		projectManager.getCurrent().getAnimation().startAnimation(this);
	}
	public void stopAnimation() {
		if(projectManager.getCurrent() == null)
			return;
		setAnimationMode(false, true);
		projectManager.getCurrent().getAnimation().terminateAnimation();
	}
	private void setAnimationMode(boolean playMode, boolean stopMode) {
		playAnimationButton.setDisable(playMode);
		stopAnimationButton.setDisable(stopMode);
		playMenuItem.setDisable(playMode);
		stopMenuItem.setDisable(stopMode);
		if(!playMode)
			setFrameOverride(-1);
	}

	public synchronized void setFrameOverride(int frameOverrideIndex) {	// negative value disables frame override
		this.frameOverrideIndex = frameOverrideIndex;
	}
	private Frame getDisplayFrame() {
		if(projectManager.getCurrent() == null)
			return null;
		if(frameOverrideIndex < 0)
			return projectManager.getCurrent().getFrame();
		return projectManager.getCurrent().getFrame(frameOverrideIndex);
	}
}
