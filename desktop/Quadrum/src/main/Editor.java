/*
 * Editor.java
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

package main;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import project.Project;
import serial.PortScanner;
import serial.SerialInterface;
import ui.editor.Clipboard;
import ui.editor.EditorController;
import ui.editor.welcomePage.WelcomePageController;

public class Editor extends Application {

	private static final String TITLE = "Quadrum";

	public static final String LOGO_LOCATION = "/res/images/logo.png",
			DEFAULT_FONT_LOCATION = "",
			STYLE_SHEET_LOCATION = "/ui/qu3dFlat.css",
			QUADRUM_URL = "https://geckoanton.github.io/quadrum/",
			HELP_URL = "https://geckoanton.github.io/quadrum/help",
			WELCOME_PAGE_URL = "https://geckoanton.github.io/quadrum/release-2.0",
			ICONS8_URL = "https://icons8.com/",
			SERIAL_LIBRARY_URL = "https://fazecast.github.io/jSerialComm/",
			geckoanton_URL = "https://github.com/geckoanton",
			CONTACT_ME_URL = "mailto:geckoanton37@gmail.com";
	private static final String EDITOR_FXML_LOCATION = "/ui/editor/editor.fxml",
			WELCOME_PAGE_FXML_LOCATION = "/ui/editor/welcomePage/welcomePage.fxml";

	Initializer initializer = new Initializer();
	private ProjectManager projectManager = new ProjectManager(this);
	public EditorController editorController = null;
	private Scene scene = null;
	private Stage stage = null;

	public SerialInterface serialInterface = new SerialInterface();
	private PortScanner portScanner = new PortScanner();
	private Clipboard clipboard = new Clipboard();

	float prevWidth = 0, prevHeight = 0;
	float prevPosX = 0, prevPosY = 0;

	public void initialize() {
		launch();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;

		FXMLLoader editorLoader = new FXMLLoader(getClass().getResource(EDITOR_FXML_LOCATION));
		Parent editorRoot = editorLoader.load();

		FXMLLoader welcomeLoader = new FXMLLoader(getClass().getResource(WELCOME_PAGE_FXML_LOCATION));
		Parent welcomeRoot = welcomeLoader.load();

		scene = new Scene(welcomeRoot);

		editorController = editorLoader.getController();
		editorController.initialize(projectManager, stage, serialInterface, portScanner, clipboard);

		((WelcomePageController) welcomeLoader.getController()).initialize(scene, editorRoot,
				editorController.menuControls);

		scene.getStylesheets().add(getClass().getResource(STYLE_SHEET_LOCATION).toExternalForm());
		stage.setTitle(TITLE);
		stage.getIcons().add(new Image(LOGO_LOCATION));
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> onClose(e));
		stage.widthProperty().addListener((obs, oldVal, newVal) -> updatePrevSize());
		stage.heightProperty().addListener((obs, oldVal, newVal) -> updatePrevSize());
		stage.xProperty().addListener((obs, oldVal, newVal) -> updatePrevPos());

		if(initializer.checkInitData()) {
			stage.setMaximized(initializer.isWindowMaximized());

			stage.setX(initializer.getWindowX());
			stage.setY(initializer.getWindowY());

			stage.setWidth(initializer.getWindowWidth());
			stage.setHeight(initializer.getWindowHeight());

			Project.setGrid(initializer.isGrid());
		}

		prevPosX = initializer.getWindowX();
		prevPosY = initializer.getWindowY();

		prevWidth = initializer.getWindowWidth();
		prevHeight = initializer.getWindowHeight();

		stage.show();

		updateProject();
	}

	public void onClose(Event event) {
		if(projectManager.getCurrent() != null)
			projectManager.getCurrent().getAnimation().terminateAnimation();

		if(!editorController.onClose()) {
			event.consume();
		}
		else {
			portScanner.terminateScan();
			serialInterface.disconnect();

			stage.hide();
			boolean stageMaximized = stage.isMaximized();
			float decorationWidth = (float) (scene.getWindow().getWidth() - scene.getWidth());
			float decorationHeight = (float) (scene.getWindow().getHeight() - scene.getHeight());

			float writePosX = (float) scene.getWindow().getX();
			float writePosY = (float) scene.getWindow().getY();

			float writeWidth = (float) scene.getWidth() + decorationWidth;
			float writeHeight = (float) scene.getHeight() + decorationHeight;

			if(stageMaximized) {
				writePosX = prevPosX;
				writePosY = prevPosY;

				writeWidth = prevWidth;
				writeHeight = prevHeight;
			}

			initializer.writeInitData(Project.isGrid(), stageMaximized, writePosX, writePosY, writeWidth, writeHeight);
		}
	}
	public void updatePrevSize() {
		if(!stage.isMaximized()) {
			float decorationWidth = (float) (scene.getWindow().getWidth() - scene.getWidth());
			float decorationHeight = (float) (scene.getWindow().getHeight() - scene.getHeight());

			prevWidth = (float) scene.getWidth() + decorationWidth;
			prevHeight = (float) scene.getHeight() + decorationHeight;
		}
	}
	public void updatePrevPos() {
		if(!stage.isMaximized() && scene.getWindow().getX() > 0 && scene.getWindow().getY() > 0) {
			prevPosX = (float) scene.getWindow().getX();
			prevPosY = (float) scene.getWindow().getY();
		}
	}

	public void updateProject() {
		editorController.updateProject();
	}
	public void updateLayout() {
		editorController.updateLayout();
	}

}
