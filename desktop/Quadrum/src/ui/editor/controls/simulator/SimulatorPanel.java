/*
 * SimulatorPanel.java
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

package ui.editor.controls.simulator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import matrixAlgebra.Mat3;
import matrixAlgebra.Vec3;
import project.Frame;
import project.Project;
import project.Voxel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SimulatorPanel {
	private static final float MOUSE_ANGLE_SENSITIVITY = 0.01f,
			VOXEL_SPACING_SIZE_RATIO = 0.3f;

	private static final int CUBE_BORDER = 50, MIN_VOXEL_SPACING = 30;

	private static final Color GRID_COLOR = new Color(0.725490196f, 0.725490196f, 0.725490196f, 1.0f);
	private static final float GRID_LINE_WIDTH = 1.0f;

	private static final Mat3 GIMBAL_ZOOM = new Mat3(50, 50, 50);
	private static final float GIMBAL_CENTER_X = 50, GIMBAL_CENTER_Y = 60,
			GIMBAL_LINE_WIDTH = 4, GIMBAL_BALL_SIZE = 10,
			GIMBAL_COORDINATE_STRING_X_OFFSET = 7, GIMBAL_COORDINATE_STRING_Y_OFFSET = -7;
	private static final Color GIMBAL_X_COLOR = Color.RED, GIMBAL_Y_COLOR = Color.LIME, GIMBAL_Z_COLOR = Color.BLUE,
			GIMBAL_BALL_COLOR = new Color(0.82f, 0.82f, 0.82f, 1.0f);
	private static final int GIMBAL_VECTORS_SIZE = 4;

	private Canvas canvas;
	private Project project;

	private float outsetMouseX = 0, outsetMouseY = 0;
	private Vec3[][][] voxelVectors = null;

	private ArrayList<GimbalVector> gimbalVectors = new ArrayList(GIMBAL_VECTORS_SIZE);

	private boolean backwardsSortX, backwardsSortY, backwardsSortZ;

	public SimulatorPanel(Project project, Canvas canvas) {
		this.canvas = canvas;
		this.project = project;
		setProject(project);

		for(int i = 0; i < GIMBAL_VECTORS_SIZE; i++) {
			gimbalVectors.add(new GimbalVector(new Vec3(), i));
		}
	}
	private void setProject(Project project) {
		if(project == null)
			return;
		voxelVectors = new Vec3[project.getSide()][project.getSide()][project.getSide()];
	}

	protected void setMouseOutsets(float mouseX, float mouseY) {
		if(project == null)
			return;

		outsetMouseX = mouseX;
		outsetMouseY = mouseY;
	}
	protected void setMouseRelative(float mouseX, float mouseY, Frame frame) {
		if(project == null)
			return;

		float horizontalAngle = (mouseX - outsetMouseX) * MOUSE_ANGLE_SENSITIVITY;
		float verticalAngle = (mouseY - outsetMouseY) * MOUSE_ANGLE_SENSITIVITY;

		project.setTransform(Mat3.rotateVertical(-verticalAngle).mult(Mat3.rotateHorizontal(-horizontalAngle).mult(project.getTransform())));

		outsetMouseX = mouseX;
		outsetMouseY = mouseY;

		updateLayout(frame);
	}

	public void updateLayout(Frame frame) {
		if(project == null) {
			draw(frame);
			return;
		}

		// voxel spacing and size computation

		float minDistance = (float) Math.min(canvas.getWidth(), canvas.getHeight());

		float maxSideLength = (float) Math.sqrt((minDistance * minDistance) / 3);    // solution to sqrt(3*maxSideLength^2) = minDistance; pythagorean theorem in 3D
		float voxelSpacing = (maxSideLength - (2 * CUBE_BORDER)) / (float) (project.getSide() - 1);
		voxelSpacing = Math.max(voxelSpacing, MIN_VOXEL_SPACING);

		float voxelSize = voxelSpacing * VOXEL_SPACING_SIZE_RATIO;
		voxelSize = Math.max(voxelSize, Voxel.MIN_VOXEL_SIZE);
		Voxel.setVoxelSize(voxelSize);

		// voxel position computation

		Mat3 voxelZoom = new Mat3(voxelSpacing, voxelSpacing, voxelSpacing);
		Mat3 voxelTransform = new Mat3().mult(project.getTransform().mult(voxelZoom));

		for (int x = 0; x < project.getSide(); x++) {
			for (int y = 0; y < project.getSide(); y++) {
				for (int z = 0; z < project.getSide(); z++) {
					float halfSideLength = (((float) project.getSide() - 1.0f) / 2.0f);
					voxelVectors[x][y][z] = voxelTransform.mult(new Vec3(x - halfSideLength, y - halfSideLength, z - halfSideLength));
				}
			}
		}

		// drawing order computation, voxel.get(2) = z (depth) value of the voxel, the smaller the closer to the viewer (screen)

		// sorting "vertical layers", can check arbitrary x, z column (in this case x = 0, z = 0)
		if (voxelVectors[0][0][0].get(2) < voxelVectors[0][project.getSide() - 1][0].get(2))
			backwardsSortY = true;    // sort layers from largest to smallest
		else
			backwardsSortY = false;    // sort layers from smallest to largest
		// sorting "horizontal rows", can check arbitrary y, z column (in this case y = 0, z = 0)
		if (voxelVectors[0][0][0].get(2) < voxelVectors[project.getSide() - 1][0][0].get(2))
			backwardsSortX = true;    // sort rows from largest to smallest
		else
			backwardsSortX = false;    // sort rows from smallest to largest
		// sorting "depth positions", can check arbitrary x, y columns (int this case x = 0, y = 0)
		if (voxelVectors[0][0][0].get(2) < voxelVectors[0][0][project.getSide() - 1].get(2))
			backwardsSortZ = true;    // sort positions from largest to smallest
		else
			backwardsSortZ = false;    // sort positions from smallest to largest

		// gimbal vectors position computation

		Mat3 gimbalTransform = new Mat3().mult(project.getTransform().mult(GIMBAL_ZOOM));

		gimbalVectors.set(0, new GimbalVector(gimbalTransform.mult(new Vec3(-0.5f, -0.5f, 0.5f)), 0));	// bottom left corner (0, 0, 0)
		gimbalVectors.set(1, new GimbalVector(gimbalTransform.mult(new Vec3(0.5f, -0.5f, 0.5f)), 1));	// x representative axis
		gimbalVectors.set(2, new GimbalVector(gimbalTransform.mult(new Vec3(-0.5f, 0.5f, 0.5f)), 2));	// z representative axis
		gimbalVectors.set(3, new GimbalVector(gimbalTransform.mult(new Vec3(-0.5f, -0.5f, -0.5f)), 3));	// y representative axis

		// sorting gimbal vectors

		Collections.sort(gimbalVectors, new Comparator<GimbalVector>() {
			@Override
			public int compare(GimbalVector gv1, GimbalVector gv2) {
				return (int) (gv2.getVector().get(2) - gv1.getVector().get(2));
			}
		});

		draw(frame);
	}
	private void draw(Frame frame) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		if(project == null || frame == null)
			return;

		float centerX = ((float) canvas.getWidth()) / 2;
		float centerY = ((float) canvas.getHeight()) / 2;

		if(project.isGrid())
			drawGrid(gc, centerX, centerY);

		drawGimbal(gc);

		for (int x = 0; x < project.getSide(); x++) {
			for (int y = 0; y < project.getSide(); y++) {
				for (int z = 0; z < project.getSide(); z++) {
					int sortedX = x, sortedY = y, sortedZ = z;
					if(backwardsSortX)
						sortedX = project.getSide() - 1 - x;
					if(backwardsSortY)
						sortedY = project.getSide() - 1 - y;
					if(backwardsSortZ)
						sortedZ = project.getSide() - 1 - z;

					frame.getVoxel(sortedX, sortedY, sortedZ).draw(gc, centerX, centerY, voxelVectors[sortedX][sortedY][sortedZ]);
				}
			}
		}
	}
	private void drawGrid(GraphicsContext gc, float centerX, float centerY) {
		gc.setStroke(GRID_COLOR);
		gc.setLineWidth(GRID_LINE_WIDTH);

		for(int a = 0; a < voxelVectors.length; a++) {
			for(int b = 0; b < voxelVectors.length; b++) {
				gc.strokeLine(centerX + voxelVectors[a][b][0].get(0), centerY - voxelVectors[a][b][0].get(1),
						centerX + voxelVectors[a][b][voxelVectors.length - 1].get(0), centerY - voxelVectors[a][b][voxelVectors.length - 1].get(1));

				gc.strokeLine(centerX + voxelVectors[a][0][b].get(0), centerY - voxelVectors[a][0][b].get(1),
						centerX + voxelVectors[a][voxelVectors.length - 1][b].get(0), centerY - voxelVectors[a][voxelVectors.length - 1][b].get(1));

				gc.strokeLine(centerX + voxelVectors[0][a][b].get(0), centerY - voxelVectors[0][a][b].get(1),
						centerX + voxelVectors[voxelVectors.length - 1][a][b].get(0), centerY - voxelVectors[voxelVectors.length - 1][a][b].get(1));
			}
		}
	}
	private void drawGimbal(GraphicsContext gc) {
		int initialIndexZero = 0;
		for(int i = 0; i < gimbalVectors.size(); i++) {
			if(gimbalVectors.get(i).getInitialIndex() == 0) {
				initialIndexZero = i;
				break;
			}
		}

		gc.setLineWidth(GIMBAL_LINE_WIDTH);

		for(int i = 0; i < gimbalVectors.size(); i++) {
			if(i == initialIndexZero)
				continue;

			String axis = "NULL";

			switch(gimbalVectors.get(i).getInitialIndex()) {
				case 1:
					gc.setStroke(GIMBAL_X_COLOR);
					gc.setFill(GIMBAL_X_COLOR);
					axis = "X";
					break;
				case 2:
					gc.setStroke(GIMBAL_Y_COLOR);
					gc.setFill(GIMBAL_Y_COLOR);
					axis = "Y";
					break;
				case 3:
					gc.setStroke(GIMBAL_Z_COLOR);
					gc.setFill(GIMBAL_Z_COLOR);
					axis = "Z";
					break;
			}

			gc.strokeLine(GIMBAL_CENTER_X + gimbalVectors.get(initialIndexZero).getVector().get(0), GIMBAL_CENTER_Y - gimbalVectors.get(initialIndexZero).getVector().get(1),
					GIMBAL_CENTER_X + gimbalVectors.get(i).getVector().get(0), GIMBAL_CENTER_Y - gimbalVectors.get(i).getVector().get(1));
			gc.fillText(axis, GIMBAL_CENTER_X + gimbalVectors.get(i).getVector().get(0) + GIMBAL_COORDINATE_STRING_X_OFFSET, GIMBAL_CENTER_Y - gimbalVectors.get(i).getVector().get(1) + GIMBAL_COORDINATE_STRING_Y_OFFSET);
		}

		gc.setFill(GIMBAL_BALL_COLOR);
		gc.fillOval(GIMBAL_CENTER_X + gimbalVectors.get(initialIndexZero).getVector().get(0) - (GIMBAL_BALL_SIZE / 2), GIMBAL_CENTER_Y - gimbalVectors.get(initialIndexZero).getVector().get(1) - (GIMBAL_BALL_SIZE / 2),
				GIMBAL_BALL_SIZE, GIMBAL_BALL_SIZE);
	}
}
