/*
 * FrameControls.java
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

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import main.ProjectManager;
import project.userActions.specialActions.actions.CopyFrameAction;
import project.userActions.specialActions.actions.LoopAction;
import project.userActions.specialActions.actions.SetCurrentFrameAction;
import project.userActions.specialActions.stackActions.*;
import ui.editor.Clipboard;
import ui.editor.EditorController;
import ui.editor.controls.ProjectControls;

public class FrameControls implements ProjectControls {
	private ProjectManager projectManager;
	private Clipboard clipboard;

	private Text timeDurationText, msDurationText;
	private ImageView timeDurationImageView;
	private Spinner<Double> frameDurationSpinner;
	private ListView<String> frameListView;
	private CheckBox loopCheckBox;
	private Button addFrameButton, removeFrameButton,
			copyFrameButton, pasteFrameButton;
	private MenuItem copyFrameMenuItem, pasteFrameMenuItem;

	public FrameControls(ProjectManager projectManager, Clipboard clipboard,
						 Text timeDurationText, Text msDurationText, ImageView timeDurationImageView, Spinner<Double> frameDurationSpinner, ListView<String> frameListView, CheckBox loopCheckBox,
						 Button addFrameButton, Button removeFrameButton, Button copyFrameButton, MenuItem copyFrameMenuItem, Button pasteFrameButton, MenuItem pasteFrameMenuItem) {
		this.projectManager = projectManager;
		this.clipboard = clipboard;

		this.timeDurationText = timeDurationText;
		this.msDurationText = msDurationText;
		this.timeDurationImageView = timeDurationImageView;
		this.frameDurationSpinner = frameDurationSpinner;
		this.frameListView = frameListView;
		this.loopCheckBox = loopCheckBox;

		this.addFrameButton = addFrameButton;
		this.removeFrameButton = removeFrameButton;
		this.copyFrameButton = copyFrameButton;
		this.copyFrameMenuItem = copyFrameMenuItem;
		this.pasteFrameButton = pasteFrameButton;
		this.pasteFrameMenuItem = pasteFrameMenuItem;
	}
	@Override
	public void updateProject() {
		updateLayout();
	}
	@Override
	public void updateLayout() {
		setNullProject(projectManager.getCurrent() == null);
		if(projectManager.getCurrent() == null)
			return;

		setSpinnerValue(projectManager.getCurrent().getFrame().getDurationInMillis());
		loopCheckBox.setSelected(projectManager.getCurrent().isLoop());
		addFrameButton.setDisable(!projectManager.getCurrent().canAddFrame());
		removeFrameButton.setDisable(!projectManager.getCurrent().canRemoveFrame());
		pasteFrameButton.setDisable(!projectManager.getCurrent().canPaste(clipboard.getFrame()));
		pasteFrameMenuItem.setDisable(!projectManager.getCurrent().canPaste(clipboard.getFrame()));
		// update frameListView to project frames
		for(int i = 0; i < projectManager.getCurrent().getFrameSize() || i < frameListView.getItems().size(); i++) {
			if(i < projectManager.getCurrent().getFrameSize() && i < frameListView.getItems().size()) {
				if(!projectManager.getCurrent().getFrame(i).getDisplayName(i).equals(frameListView.getItems().get(i)))
					frameListView.getItems().set(i, projectManager.getCurrent().getFrame(i).getDisplayName(i));
			}
			else if(i < projectManager.getCurrent().getFrameSize()) {
				frameListView.getItems().add(projectManager.getCurrent().getFrame(i).getDisplayName(i));
			}
			else {
				frameListView.getItems().remove(i);
				i--;
			}
		}
		frameListView.getSelectionModel().select(projectManager.getCurrent().getFrameIndex());
	}
	private void setNullProject(boolean nullProject) {
		float opacity = 1.0f;
		if(nullProject) {
			opacity = EditorController.DISABLED_OPACITY;
			frameListView.getItems().clear();
		}
		timeDurationText.setOpacity(opacity);
		msDurationText.setOpacity(opacity);
		timeDurationImageView.setOpacity(opacity);

		frameDurationSpinner.setDisable(nullProject);
		frameListView.setDisable(nullProject);
		loopCheckBox.setDisable(nullProject);
		addFrameButton.setDisable(nullProject);
		removeFrameButton.setDisable(nullProject);
		copyFrameButton.setDisable(nullProject);
		copyFrameMenuItem.setDisable(nullProject);
		pasteFrameButton.setDisable(nullProject);
		pasteFrameMenuItem.setDisable(nullProject);
	}

	private boolean ignoreNextFrameSet = false;	// used to not trigger updateFrameDuration when user did not set duration value
	public void updateFrameDuration(float oldValue, float newValue) {
		if(projectManager.getCurrent() == null)
			return;
		if(!ignoreNextFrameSet)
			projectManager.getCurrent().executeOnStack(new FrameDurationAction(projectManager.getCurrent().getFrameIndex(), oldValue, newValue));
		ignoreNextFrameSet = false;
	}
	private void setSpinnerValue(float value) {
		if(value == frameDurationSpinner.getValueFactory().getValue())
			return;
		ignoreNextFrameSet = frameDurationSpinner.getValueFactory().getValue() != value;
		frameDurationSpinner.getValueFactory().setValue((double) value);
	}

	public void setLoop(boolean state) {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().execute(new LoopAction(state));
	}

	public void updateCurrentFrame(int currentFrame) {
		if(projectManager.getCurrent() == null)
			return;
		if(projectManager.getCurrent().getFrameIndex() == currentFrame)
			return;
		projectManager.getCurrent().execute(new SetCurrentFrameAction(currentFrame));
	}

	public void setFrameListViewEdit(int editIndex) {
		//frameListView.getItems().set(editIndex, projectManager.getCurrent().getFrame(editIndex).getName());
		// (bug) set editing text to just frame name (without [1])
	}
	public void setFrameName(String name) {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().executeOnStack(new FrameNameAction(name, projectManager.getCurrent().getFrameIndex()));
	}

	public void addFrame() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().executeOnStack(new AddFrameAction(projectManager.getCurrent().getFrameIndex() + 1));
	}
	public void removeFrame() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().executeOnStack(new RemoveFrameAction(projectManager.getCurrent().getFrameIndex()));
	}

	public void copyFrame() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().execute(new CopyFrameAction(clipboard));
	}
	public void pasteFrame() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().executeOnStack(new PasteFrameAction(clipboard, projectManager.getCurrent().getFrameIndex()));
	}
}
