/*
 * PaintPanel.java
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

package ui.editor.controls.paint;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import project.Project;
import project.Voxel;
import java.util.ArrayList;

public class PaintPanel {
	private static final int MIN_PLANE_BORDER = 20;

	private Canvas canvas;
	private Project project;
	private ArrayList<Plane> planes = null;

	private int page = 0;	// display page with size capacity starting from index in planes page
	private int capacity = 0;

	private boolean mousePressedState = false;
	private float mouseX = 0, mouseY = 0;

	private int previousVoxel = -1;	// records previous voxel to avoid double performing action on voxel button

	public PaintPanel(Project project, Canvas canvas) {
		this.project = project;
		this.canvas = canvas;
		setProject(project);
	}
	private void setProject(Project project) {
		if(project == null) {
			planes = null;
			return;
		}
		planes = new ArrayList<>(project.getSide());
		for(int i = 0; i < project.getSide(); i++)
			planes.add(new Plane(project, i));
	}

	public void nextPage() {
		if(page + capacity >= planes.size())
			return;
		page += capacity;
		updateLayout();
	}
	public void previousPage() {
		if(page <= 0)
			return;
		if(page - capacity < 0)
			page = 0;
		else
			page -= capacity;
		updateLayout();
	}
	public void displayPlane(int index) {
		int diff = index - page - capacity + 1;
		page += Math.ceil((float) diff / (float) capacity) * capacity;
		if(page < 0)
			page = 0;
		else if(page > planes.size() - capacity)
			page = planes.size()- capacity;
		updateLayout();
	}

	protected void mouseAction(PaintControls paintControls, float mouseX, float mouseY, boolean mousePressedState) {
		if(project == null)
			return;
		int currentVoxel = -1;
		for(int i = page; i < planes.size() + capacity && i < planes.size(); i++) {
			currentVoxel = planes.get(i).mouseAction(paintControls, project.getFrameIndex(), mouseX, mouseY, previousVoxel);
			if(currentVoxel >= 0)
				break;
		}
		previousVoxel = currentVoxel;
		mouseState(mouseX, mouseY, mousePressedState);
	}
	protected void mouseState(float mouseX, float mouseY, boolean mousePressedState) {
		if(!mousePressedState)
			previousVoxel = -1;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.mousePressedState = mousePressedState;
		draw();
	}
	private void resetMouseState() {
		this.mouseX = -1;
		this.mouseY = -1;
		this.mousePressedState = false;
	}

	// used to set next / previous button enable states
	public boolean isDisableNext() {
		if(project == null)
			return true;
		return page + capacity >= planes.size();
	}
	public boolean isDisablePrevious() {
		if(project == null)
			return true;
		return page <= 0;
	}

	public void updateLayout() {
		if(project == null) {
			draw();
			return;
		}

		int minPlaneSize = (project.getSide() * Voxel.MIN_VOXEL_BUTTON_SIZE) + ((project.getSide() - 1) * Plane.VOXEL_BUTTON_BORDER);	// minimum side length (in pixels of every plane)

		float canvasHeight = (float) canvas.getHeight();
		float canvasWidth = (float) canvas.getWidth();

		int columns = (int) Math.floor((canvasWidth - MIN_PLANE_BORDER) / ((float) (minPlaneSize + MIN_PLANE_BORDER)));	// fit as many columns as possible given minPlaneSize
		int rows = (int) Math.floor((canvasHeight - MIN_PLANE_BORDER) / ((float) (minPlaneSize + MIN_PLANE_BORDER)));	// fit as many rows as possible given minPlaneSize

		if(columns <= 0) {
			// continue layout as if canvas width is larger
			columns = 1;
			canvasWidth = 2 * MIN_PLANE_BORDER + minPlaneSize;
		}
		if(rows <= 0) {
			// continue layout as if canvas height is larger
			rows = 1;
			canvasHeight = 2 * MIN_PLANE_BORDER + minPlaneSize;
		}

		int maxCapacity = columns * rows;

		if(planes.size() < maxCapacity) {
			float ratio = (canvasHeight - (float) MIN_PLANE_BORDER) / (canvasWidth - (float) MIN_PLANE_BORDER);
			float width = (float) Math.sqrt(((float) planes.size()) / ratio);	// solution to: width * (width * ratio) = planes.size
			float height = width * ratio;

			float floorWidth = (float) Math.max(Math.floor(width), 1);	// floorWidth cannot be 0
			float floorHeight = (float) Math.max(Math.floor(height), 1);	// floorHeight cannot be 0

			int topWidth = (int) Math.ceil(planes.size() / floorHeight);	// ceiling solution to: WIDTH * floorHeight = planes.size()
			int topHeight = (int) Math.ceil(planes.size() / floorWidth);	// ceiling solution to: floorWidth * HEIGHT = planes.size()

			float ceilWidthSize = (canvasWidth - ((((float) topWidth) + 1) * MIN_PLANE_BORDER)) / ((float) topWidth);	// get size of every plane given topWidth (theoretical number of columns)
			float ceilHeightSize = (canvasHeight - ((((float) topHeight) + 1) * MIN_PLANE_BORDER)) / ((float) topHeight);	// get size of every plane given topHeight (theoretical number of rows)

			float a = (canvasWidth - ((((float) Math.ceil(width)) + 1) * MIN_PLANE_BORDER)) / ((float) Math.ceil(width));
			float b = (canvasHeight - ((((float) Math.ceil(height)) + 1) * MIN_PLANE_BORDER)) / ((float) Math.ceil(height));
			float ceilBothSize = Math.min(a, b);	// get size of plane is both width and height are ceiled

			if(ceilWidthSize >= ceilHeightSize && topWidth <= columns && floorHeight <= rows) {
				if(ceilBothSize > ceilWidthSize && Math.ceil(width) <= columns && Math.ceil(height) <= rows) {
					columns = (int) Math.ceil(width);
					rows = (int) Math.ceil(height);
				}
				else {
					columns = topWidth;
					rows = (int) floorHeight;
				}
			}
			else if(ceilHeightSize > ceilWidthSize && floorWidth <= columns && topHeight <= rows) {
				if(ceilBothSize > ceilHeightSize && Math.ceil(width) <= columns && Math.ceil(height) <= rows) {
					columns = (int) Math.ceil(width);
					rows = (int) Math.ceil(height);
				}
				else {
					columns = (int) floorWidth;
					rows = topHeight;
				}
			}
			else if(Math.ceil(width) <= columns && Math.ceil(height) <= rows) {
				columns = (int) Math.ceil(width);
				rows = (int) Math.ceil(height);
			}
		}

		// rows and columns are now set to fit as many planes as possible, while having the greatest plane area possible

		capacity = rows * columns;

		if(capacity >= planes.size())
			page = 0;
		else if(page > planes.size() - capacity)
			page = planes.size() - capacity;

		float horizontalSize = (canvasWidth - ((columns + 1) * MIN_PLANE_BORDER)) / ((float) columns);
		float verticalSize = (canvasHeight - ((rows + 1) * MIN_PLANE_BORDER)) / ((float) rows);
		float planeSize = Math.min(horizontalSize, verticalSize);

		float horizontalSpacing = ((canvasWidth - (planeSize * columns))) / ((float) (columns + 1));
		float verticalSpacing = ((canvasHeight - (planeSize * rows))) / ((float) (rows + 1));
		for(int i = page; i < planes.size() + capacity && i < planes.size(); i++) {
			int x = (i - page) % columns;
			int y = (int) Math.floor(((float) (i - page)) / ((float) columns));
			planes.get(i).setPosSize(horizontalSpacing + (horizontalSpacing + planeSize) * ((float) x), verticalSpacing + (verticalSpacing + planeSize) * ((float) y), planeSize);
		}

		resetMouseState();

		draw();
	}
	private void draw() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		if(project == null)
			return;

		for(int i = page; i < planes.size() + capacity && i < planes.size(); i++)
			planes.get(i).draw(gc, mouseX, mouseY, mousePressedState);
	}
}
