/*
 * FileUserInterface.java
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

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import main.ProjectManager;
import project.Project;
import java.io.File;

public class FileUserInterface {
	public static boolean saveProjectFileChooser(Project project, Stage stage) {
		if(project == null || stage == null)
			return false;

		if(project.hasFilePath()) {
			if(project.save())
				return true;
		}
		else {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialFileName(project.getName() + "." + Project.FILE_EXTENSION);

			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Project.FILE_TYPE_DESCRIPTION, "*." + Project.FILE_EXTENSION);
			fileChooser.getExtensionFilters().add(extFilter);

			File file = fileChooser.showSaveDialog(stage);

			if(file == null)
				return false;

			if (project.save(file.getAbsolutePath()))
				return true;
		}

		DialogBoxes.errorSaveDialogBox(project.getName());
		return false;
	}
	public static void openProjectFileChooser(ProjectManager projectManager, Stage stage) {
		if(stage == null)
			return;

		FileChooser fileChooser = new FileChooser();

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Project.FILE_TYPE_DESCRIPTION, "*." + Project.FILE_EXTENSION);
		fileChooser.getExtensionFilters().add(extFilter);

		File file = fileChooser.showOpenDialog(stage);

		if(file == null)
			return;

		if(projectManager.addLoad(file.getAbsolutePath()))
			return;

		DialogBoxes.errorOpenDialogBox(file.getAbsolutePath());
	}
}
