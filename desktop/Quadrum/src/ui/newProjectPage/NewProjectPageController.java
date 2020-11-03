/*
 * NewProjectPageController.java
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

package ui.newProjectPage;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.ProjectManager;

public class NewProjectPageController {
	// fxml related

	@FXML
	private AnchorPane mainAnchorPane;

	@FXML
	private Pane sideSelectPane;
	@FXML
	private Canvas sideSelectCanvas;

	@FXML
	private Text sideText;
	@FXML
	private TextField projectNameTextField;

	public void initialize() {
		sideSelectPane.widthProperty().addListener(e -> updateSideSelectCanvasSize());
		sideSelectPane.heightProperty().addListener(e -> updateSideSelectCanvasSize());

		// add mouse actions to newProjectControls
		mainAnchorPane.addEventFilter(MouseEvent.MOUSE_MOVED, e -> newProjectControls.mouseMoved(	// give mouse moved position relative to anchor pane
				(float) (e.getSceneX() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinY())));
		sideSelectPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> newProjectControls.mousePressed(	// give mouse pressed position relative to anchor pane
				(float) (e.getSceneX() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinY())));
		sideSelectPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> newProjectControls.mouseDragged(	// give mouse dragged position relative to anchor pane
				(float) (e.getSceneX() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinY())));
		sideSelectPane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> newProjectControls.mouseReleased(	// give mouse released position relative to anchor pane
				(float) (e.getSceneX() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - sideSelectPane.localToScene(sideSelectPane.getBoundsInLocal()).getMinY())));
	}
	private void updateSideSelectCanvasSize() {
		sideSelectCanvas.setWidth(sideSelectPane.getWidth());
		sideSelectCanvas.setHeight(sideSelectPane.getHeight());
		if(newProjectControls != null)
			newProjectControls.updateLayout();
	}

	@FXML
	private void decrementSelectedSideAction() {
		if(newProjectControls == null)
			return;
		newProjectControls.decrementSelectedSide();
	}
	@FXML
	private void incrementSelectedSideAction() {
		if(newProjectControls == null)
			return;
		newProjectControls.incrementSelectedSide();
	}

	@FXML
	public void createAction() {
		if(newProjectControls == null)
			return;
		newProjectControls.createProject();
		stage.close();
	}
	@FXML
	public void cancelAction() {
		stage.close();
	}

	// non-fxml

	private NewProjectControls newProjectControls = null;

	private Stage stage;

	public void initialize(ProjectManager projectManager, Stage stage) {
		newProjectControls = new NewProjectControls(projectManager, sideSelectCanvas, projectNameTextField, sideText);
		this.stage = stage;
	}

}
