/*
 * ProjectManager.java
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

import project.Project;
import java.util.ArrayList;

public class ProjectManager {

	private Editor editor;
	private ArrayList<Project> projects = new ArrayList<>();
	private int current = 0;

	public ProjectManager(Editor editor) {
		this.editor = editor;
	}

	public void setCurrent(int current) {
		if(getCurrent() != null)
			getCurrent().getAnimation().terminateAnimation();
		this.current = current;
		editor.updateProject();
	}
	public Project getCurrent() {
		if(current < 0 || current >= projects.size())
			return null;
		return projects.get(current);
	}
	public Project get(int index) {
		return projects.get(index);
	}

	public int getCurrentIndex() {
		return current;
	}
	public int getProjectSize() {
		return projects.size();
	}

	public void addNew(String name, int side) {
		projects.add(new Project(editor, name, side));
		setCurrent(projects.size() - 1);	// also updates projects to ui
	}
	public boolean addLoad(String path) {
		for(int i = 0; i < projects.size(); i++) {
			if(path.equals(projects.get(i).getFilePath())) {
				setCurrent(i);
				return true;
			}
		}

		Project projectPath = Project.loadProject(editor, path);
		if(projectPath == null)
			return false;
		projects.add(projectPath);
		setCurrent(projects.size() - 1);	// also updates projects to ui
		return true;
	}
	public void remove(int index) {
		if(index < 0 || index >= projects.size())
			return;
		projects.remove(index);
		if(current >= projects.size())
			setCurrent(Math.max(index - 1, 0));	// also updates projects to ui
		else
			editor.updateProject();
	}
}
