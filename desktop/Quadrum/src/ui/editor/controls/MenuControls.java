/*
 * MenuControls.java
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

package ui.editor.controls;

import javafx.event.Event;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import main.Editor;
import main.GeneralMethods;
import main.ProjectManager;
import project.userActions.specialActions.actions.GridAction;
import project.userActions.specialActions.actions.SaveAction;
import ui.aboutPage.AboutPage;
import ui.editor.DialogBoxes;
import ui.editor.FileUserInterface;
import ui.newProjectPage.NewProjectPage;

public class MenuControls implements ProjectControls {
	private ProjectManager projectManager;
	private Stage stage;

	private MenuItem saveMenuItem,
			undoMenuItem, redoMenuItem;
	private CheckMenuItem gridCheckMenuItem;

	private TabPane projectTabPane, serialPaintTabPane;

	public MenuControls(ProjectManager projectManager, Stage stage,
						MenuItem saveMenuItem,
						MenuItem undoMenuItem, MenuItem redoMenuItem,
						CheckMenuItem gridCheckMenuItem,
						TabPane projectTabPane, TabPane serialPaintTabPane) {
		this.projectManager = projectManager;
		this.stage = stage;

		this.saveMenuItem = saveMenuItem;

		this.undoMenuItem = undoMenuItem;
		this.redoMenuItem = redoMenuItem;

		this.gridCheckMenuItem = gridCheckMenuItem;

		this.projectTabPane = projectTabPane;
		this.serialPaintTabPane = serialPaintTabPane;
	}

	@Override
	public void updateProject() {
		// update projectTabPane
		for(int i = 0; i < projectManager.getProjectSize() || i < projectTabPane.getTabs().size(); i++) {
			if(i < projectManager.getProjectSize() && i < projectTabPane.getTabs().size()) {
				if(!projectManager.get(i).getName().equals(projectTabPane.getTabs().get(i).getText())) {
					projectTabPane.getTabs().remove(i);
					i--;
				}
				else {
					projectTabPane.getTabs().get(i).setId(Integer.toString(i));
				}
			}
			else if(i < projectManager.getProjectSize()) {
				Tab tab = new Tab(projectManager.get(i).getName());
				tab.setId(Integer.toString(i));
				tab.setOnCloseRequest(e -> projectTabCloseAction(e, Integer.parseInt(tab.getId())));
				projectTabPane.getTabs().add(tab);
			}
			else {
				projectTabPane.getTabs().remove(i);
			}
		}
		if(projectManager.getCurrentIndex() < projectTabPane.getTabs().size())
			projectTabPane.getSelectionModel().select(projectManager.getCurrentIndex());

		updateLayout();
	}
	@Override
	public void updateLayout() {
		setNullProject(projectManager.getCurrent() == null);

		if(projectManager.getCurrent() == null)
			return;

		saveMenuItem.setDisable(!projectManager.getCurrent().isModified());
		undoMenuItem.setDisable(!projectManager.getCurrent().canUndo());
		redoMenuItem.setDisable(!projectManager.getCurrent().canRedo());
		gridCheckMenuItem.setSelected(projectManager.getCurrent().isGrid());
	}
	boolean previouslyNullProject = false;	// keeps track if the last loaded project was null
	private void setNullProject(boolean nullProject) {
		saveMenuItem.setDisable(nullProject);
		undoMenuItem.setDisable(nullProject);
		redoMenuItem.setDisable(nullProject);
		gridCheckMenuItem.setDisable(nullProject);

		if(nullProject) {
			serialPaintTabPane.getSelectionModel().select(1);    // select serial tab
			serialPaintTabPane.getTabs().get(0).setDisable(true);
			serialPaintTabPane.getTabs().get(2).setDisable(true);
			previouslyNullProject = true;
		}
		else if(previouslyNullProject) {
			serialPaintTabPane.getSelectionModel().select(0);    // select paint tab
			serialPaintTabPane.getTabs().get(0).setDisable(false);
			serialPaintTabPane.getTabs().get(2).setDisable(false);
			previouslyNullProject = false;
		}
	}

	public void newProjectAction() {
		if(projectManager.getCurrent() != null)
			projectManager.getCurrent().execute(null);	// execute null action to update layout, terminate animation etc.
		new NewProjectPage(projectManager);
	}
	public void openProjectAction() {
		if(projectManager.getCurrent() != null)
			projectManager.getCurrent().execute(null);	// execute null action to update layout, terminate animation etc.
		FileUserInterface.openProjectFileChooser(projectManager, stage);
	}
	public void saveProjectAction() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().execute(new SaveAction(stage));
	}

	public void undoAction() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().undo();
	}
	public void redoAction() {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().redo();
	}

	public void setGrid(boolean state) {
		if(projectManager.getCurrent() == null)
			return;
		projectManager.getCurrent().execute(new GridAction(state));
	}

	public void helpAction() {
		GeneralMethods.openUrl(Editor.HELP_URL);
	}
	public void aboutAction() {
		new AboutPage();
	}

	public void projectTabPaneAction() {
		projectManager.setCurrent(projectTabPane.getSelectionModel().getSelectedIndex());
	}
	public void projectTabCloseAction(Event event, int index) {
		if (!projectManager.get(index).isModified()) {
			projectManager.remove(index);
			return;
		}
		switch (DialogBoxes.saveRequest(projectManager.get(index).getName())) {
			case YES:
				if (FileUserInterface.saveProjectFileChooser(projectManager.get(index), stage))
					projectManager.remove(index);
				else
					event.consume();
				break;
			case NO:
				projectManager.remove(index);
				break;
			case CANCEL:
				event.consume();
				break;
		}
	}
	public boolean closeAllProjects() {
		updateProject();
		boolean stopCloseFlag = false;
		int startProjectSize = projectManager.getProjectSize();
		for(int i = 0; i < startProjectSize; i++) {
			if(stopCloseFlag)
				break;
			if (!projectManager.get(0).isModified()) {
				projectManager.remove(0);
			}
			else {
				switch (DialogBoxes.saveRequest(projectManager.get(0).getName())) {
					case YES:
						if (FileUserInterface.saveProjectFileChooser(projectManager.get(0), stage))
							projectManager.remove(0);
						else
							stopCloseFlag = true;
						break;
					case NO:
						projectManager.remove(0);
						break;
					case CANCEL:
						stopCloseFlag = true;
						break;
				}
			}
		}
		return !stopCloseFlag;
	}
}
