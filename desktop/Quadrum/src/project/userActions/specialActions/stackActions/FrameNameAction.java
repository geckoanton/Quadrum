/*
 * FrameNameAction.java
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

import project.Project;
import project.userActions.StackAction;

public class FrameNameAction implements StackAction {
	private String oldName, newName;
	private int frameIndex;

	public FrameNameAction(String newName, int frameIndex) {
		this.newName = newName;
		this.frameIndex = frameIndex;
	}

	@Override
	public boolean execute(Project project) {
		oldName = project.getFrame(frameIndex).getName();
		if(oldName.equals(newName))
			return false;
		project.getFrame(frameIndex).setName(newName);
		project.setCurrentFrame(frameIndex);
		return true;
	}
	@Override
	public void undo(Project project) {
		project.getFrame(frameIndex).setName(oldName);
		project.setCurrentFrame(frameIndex);
	}
}
