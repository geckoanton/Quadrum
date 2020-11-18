/*
 * NewProjectPage.java
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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.Editor;
import main.ProjectManager;

public class NewProjectPage extends Stage {
	private static final String TITLE = "New Project",
			NEW_PROJECT_PAGE_FXML_LOCATION = "/ui/newProjectPage/newProjectPage.fxml";

	public NewProjectPage(ProjectManager projectManager) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(NEW_PROJECT_PAGE_FXML_LOCATION));
		Parent root; Scene scene;
		try {
			root = loader.load();
			scene = new Scene(root);
		}
		catch(Exception e) { return; }

		((NewProjectPageController) loader.getController()).initialize(projectManager, this);

		scene.getStylesheets().add(getClass().getResource(Editor.STYLE_SHEET_LOCATION).toExternalForm());
		this.setTitle(TITLE);
		this.getIcons().add(new Image(Editor.LOGO_LOCATION));
		this.setScene(scene);
		this.setResizable(false);
		this.initModality(Modality.APPLICATION_MODAL);

		this.show();
	}
}
