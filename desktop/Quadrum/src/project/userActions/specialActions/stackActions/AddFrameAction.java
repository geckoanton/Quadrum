/*
 * AddFrameAction.java
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

package project.userActions.specialActions.stackActions;

import project.Frame;
import project.Project;
import project.userActions.StackAction;

public class AddFrameAction implements StackAction {
	private Frame frame = null;
	private int addedIndex;

	public AddFrameAction(int addedIndex) {
		this.addedIndex = addedIndex;
	}

	@Override
	public boolean execute(Project project) {
		boolean ret = project.addFrame(frame, addedIndex);
		project.setCurrentFrame(addedIndex);
		return ret;
	}
	@Override
	public void undo(Project project) {
		frame = project.removeFrame(addedIndex);
		project.setCurrentFrame(addedIndex - 1);
	}
}
