/*
 * ActionHandler.java
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

package project.userActions;

import main.Editor;
import project.Project;
import java.util.Stack;

public class ActionHandler {
	private static final int MAX_STACK_SIZE = 65536;

	private Project project;
	private Editor editor;

	private Stack<StackAction> actionStack = new Stack<>();
	private Stack<StackAction> redoStack = new Stack<>();

	public ActionHandler(Project project, Editor editor) {
		this.project = project;
		this.editor = editor;
	}

	public void execute(Action action) {
		project.getAnimation().terminateAnimation();
		if(action != null) {
			project.setModified(true);
			action.execute(project);
		}
		editor.updateLayout();
	}
	public void executeOnStack(StackAction action) {
		if(!action.execute(project))
			return;
		redoStack.clear();
		project.setModified(true);
		project.getAnimation().terminateAnimation();
		if(actionStack.size() > MAX_STACK_SIZE)
			actionStack.remove(0);	// remove first oldest action on stack
		actionStack.push(action);
		editor.updateLayout();
	}

	public void undo() {
		if(actionStack.isEmpty())
			return;
		redoStack.push(actionStack.peek());
		actionStack.pop().undo(project);
		editor.updateLayout();
	}
	public void redo() {
		if(redoStack.isEmpty())
			return;
		actionStack.push(redoStack.peek());
		redoStack.pop().execute(project);
		editor.updateLayout();
	}

	public boolean canUndo() {
		return !actionStack.isEmpty();
	}
	public boolean canRedo() {
		return !redoStack.isEmpty();
	}
}
