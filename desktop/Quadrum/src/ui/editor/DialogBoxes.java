/*
 * DialogBoxes.java
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

package ui.editor;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.Editor;
import java.util.Optional;

public class DialogBoxes {
	public enum RequestAnswer {YES, NO, CANCEL}

	public static RequestAnswer saveRequest(String projectName) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

		alert.getDialogPane().getScene().getStylesheets().add(Editor.STYLE_SHEET_LOCATION);
		alertStage.getIcons().add(new Image(Editor.LOGO_LOCATION));
		alert.setTitle("Save Project");
		alert.setHeaderText("Would you like to save project?");
		alert.setContentText("Project: '" + projectName + "'");

		ButtonType yesButton = new ButtonType("Save");
		ButtonType noButton = new ButtonType("Do Not Save");
		ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get().equals(yesButton))
			return RequestAnswer.YES;
		else if (result.get().equals(noButton))
			return RequestAnswer.NO;
		return RequestAnswer.CANCEL;
	}

	public static void errorSaveDialogBox(String projectName) {
		error("Save Failed", "Failed to save project.", "Project: '" + projectName + "'");
	}
	public static void errorOpenDialogBox(String projectPath) {
		error("Open Failed", "Failed to open project path.", "Path: '" + projectPath + "'");
	}
	private static void error(String title, String message, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();

		alert.getDialogPane().getScene().getStylesheets().add(Editor.STYLE_SHEET_LOCATION);
		alertStage.getIcons().add(new Image(Editor.LOGO_LOCATION));
		alert.setTitle(title);
		alert.setHeaderText(message);
		alert.setContentText(content);
		alert.getButtonTypes().setAll(new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE));
		alert.showAndWait();
	}
}
