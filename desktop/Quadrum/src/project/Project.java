/*
 * Project.java
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

package project;

import javafx.scene.paint.Color;
import main.Editor;
import main.GeneralMethods;
import matrixAlgebra.Mat3;
import project.userActions.Action;
import project.userActions.ActionHandler;
import project.userActions.StackAction;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Project {
	public static final String DEFAULT_PROJECT_NAME = "Untitled Project";
	private static final int MAX_FRAME_COUNT = 65535,
			MAX_SIDE = 32;
	private static final float START_HORIZONTAL_ROTATION = -0.392699082f,
			START_VERTICAL_ROTATION = 2.74889357159f;
	private static final Color START_PAINT_COLOR = new Color(1.0f, 0.0f, 0.0f, 1.0f);

	private String name = DEFAULT_PROJECT_NAME;
	private ActionHandler actionHandler;
	private int side,
			currentFrame = 0;
	private Mat3 transform;
	private ArrayList<Frame> frames = new ArrayList<>();

	private boolean loop = true;
	private static boolean grid = true;
	private Color paintColor = START_PAINT_COLOR;

	private Animation animation;

	private static int totalUnnamedProjects = 0;

	public Project(Editor editor, String name, int side) {
		animation = new Animation(this, editor.serialInterface);

		side = Math.max(Math.min(side, MAX_SIDE), 1);
		this.side = side;
		frames.add(new Frame(side));

		transform = Mat3.rotateVertical(START_VERTICAL_ROTATION).mult(Mat3.rotateHorizontal(START_HORIZONTAL_ROTATION));

		actionHandler = new ActionHandler(this, editor);
		setName(name); }

	public void setName(String name) {
		this.name = name;
		if(name.equals("") || name.equals(DEFAULT_PROJECT_NAME)) {
			totalUnnamedProjects++;
			if(totalUnnamedProjects > 1)
				this.name = DEFAULT_PROJECT_NAME + " (" + totalUnnamedProjects + ")";
			else
				this.name = DEFAULT_PROJECT_NAME;
		}
	}
	public String getName() {
		return name;
	}

	public void execute(Action action) { actionHandler.execute(action); }
	public void executeOnStack(StackAction stackAction) { actionHandler.executeOnStack(stackAction); }
	public void undo() { actionHandler.undo(); }
	public void redo() { actionHandler.redo(); }
	public boolean canUndo() { return actionHandler.canUndo(); }
	public boolean canRedo() { return actionHandler.canRedo(); }

	public int getSide() {
		return side;
	}

	public synchronized Frame getFrame() {
		if (currentFrame < 0 || currentFrame >= frames.size())
			return null;
		return frames.get(currentFrame);
	}
	public synchronized Frame getFrame(int index) {
		if (index < 0 || index >= frames.size())
			return null;
		return frames.get(index);
	}
	public synchronized int getFrameIndex() {
		return currentFrame;
	}
	public synchronized int getFrameSize() { return frames.size(); }

	public boolean addFrame(Frame frame, int index) {
		if(index < 0 || index > frames.size() || frames.size() >= MAX_FRAME_COUNT)
			return false;
		if(frame == null)
			frame = new Frame(getSide());
		frames.add(index, frame);
		return true;
	}
	public Frame replaceFrame(Frame frame, int index) {
		if(frame == null)
			return null;
		if(index < 0 || index >= frames.size() || frame.getSide() != getSide() || frame.equals(frames.get(index)))
			return null;
		Frame ret = getFrame(index);
		frames.set(index, frame);
		return ret;
	}
	public Frame removeFrame(int index) {
		if(index < 0 || index >= frames.size() || frames.size() <= 1)
			return null;
		Frame ret = getFrame(index);
		frames.remove(index);
		return ret;
	}

	public boolean canAddFrame() {
		return frames.size() < MAX_FRAME_COUNT;
	}
	public boolean canRemoveFrame() {
		return frames.size() > 1;
	}
	public boolean canPaste(Frame frame) {
		if(frame == null)
			return false;
		return frame.getSide() == getSide() && !frame.equals(getFrame());
	}

	public void setCurrentFrame(int currentFrame) {
		currentFrame = Math.min(Math.max(0, currentFrame), frames.size() - 1);
		this.currentFrame = currentFrame;
	}

	public Animation getAnimation() {
		return animation;
	}

	public Mat3 getTransform() {
		return transform;
	}
	public void setTransform(Mat3 transform) {
		this.transform = transform;
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	public boolean isLoop() {
		return loop;
	}

	public static void setGrid(boolean grid) {
		Project.grid = grid;
	}
	public static boolean isGrid() {
		return grid;
	}

	public void setPaintColor(Color paintColor) {
		this.paintColor = paintColor;
	}
	public Color getPaintColor() {
		return paintColor;
	}

	/*
		============================ qu3d file structure ==============================
			        BYTE               |         DESCRIPTION          |     DATA
	file                               |                              |
		0 - 7                          |  qu3d file signature         |  71 75 3D 37...     }
		8 - 8                          |  cubeSideLength              |  BYTE               }
		9 - 10                         |  number of frames            |  2 * BYTE           }
	options                            |                              |                     }  BYTES_TO_FRAMES
		11 - 11                        |  loop option                 |  ff or 00           }
		12 - 15                        |  paint color                 |  R G B A            }
		16 - 16                        |  lastly edited frame         |  BYTE               }
	frames                             |                              |
	    17 - 20                        |  frame duration time         |  FLOAT              }
	    21 - 36                        |  frame name                  |  STRING             }
		37 - (3N + 36)                 |  cube voxel color (frame 0)  |  R G ... G B        }
		(3N + 37) - (6N + 36)          |  cube voxel color (frame 1)  |  R G ... G B        }
		                                        .                                           }  ALL FRAMES
		                                        .                                           }
		                                        .                                           }
		(AN + K - 20) - (BN + K - 15)  |  frame duration time         |  FLOAT              }
		(AN + K - 16) - (BN + K - 1)   |  frame name                  |  STRING             }
		(AN + K) - (BN + K)            |  cube voxel color (frame c)  |  R G ... G B        }
		===============================================================================
	NOTE: N is cubeSideLength^3
	*/

	public static final String FILE_EXTENSION = "qu3d";
	public static final String FILE_TYPE_DESCRIPTION = "QU3D file (*.qu3d)";

	private static final int BYTES_IN_FRAME_DURATION = 4;
	public static final int FRAME_NAME_LENGTH = 32;
	private static final byte[] QU3D_SIGNATURE = {0x71, 0x75, 0x3D, 0x37, 0x4C, 0x45, 0x4F, 0x3E};

	private boolean modified = false;
	private String filePath = null;

	public String getFilePath() {
		return filePath;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}
	public boolean isModified() {
		return modified;
	}

	public boolean hasFilePath() {
		return filePath != null;
	}
	private void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean save() {
		return save(filePath);
	}
	public boolean save(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			ZipOutputStream outputStream = new ZipOutputStream(fos);
			outputStream.putNextEntry(new ZipEntry("mainEntry"));

			// write file signature, cube side and number of frames
			outputStream.write(QU3D_SIGNATURE, 0, QU3D_SIGNATURE.length);	// file signature
			outputStream.write(new byte[]{Integer.valueOf(getSide()).byteValue()}, 0, 1);	// cubeSideLength
			outputStream.write(ByteBuffer.allocate(4).putInt(frames.size()).array(), 0, 4);
			// write options
			outputStream.write(new byte[]{(byte) (isLoop() ? 0xff : 0x0)}, 0, 1);
			byte[] paintColor = new byte[4];
			paintColor[0] = Integer.valueOf((int) (Math.round(getPaintColor().getRed() * 255))).byteValue();
			paintColor[1] = Integer.valueOf((int) (Math.round(getPaintColor().getGreen() * 255))).byteValue();
			paintColor[2] = Integer.valueOf((int) (Math.round(getPaintColor().getBlue() * 255))).byteValue();
			paintColor[3] = Integer.valueOf((int) (Math.round(getPaintColor().getOpacity() * 255))).byteValue();
			outputStream.write(paintColor, 0, paintColor.length);
			outputStream.write(ByteBuffer.allocate(4).putInt(getFrameIndex()).array(), 0, 4);
			// write cube frame data
			byte[] frameData = new byte[(int) (3 * Math.pow(getSide(), 3))];
			for(int i = 0; i < frames.size(); i++) {	// loop through every frame
				outputStream.write(ByteBuffer.allocate(4).putFloat(getFrame(i).getDurationInMillis()).array(), 0, 4);

				int frameNameLength = Math.min(getFrame(i).getName().length(), 255);
				outputStream.write(new byte[]{Integer.valueOf(frameNameLength).byteValue()}, 0, 1);
				outputStream.write(GeneralMethods.stringToByteArray(getFrame(i).getName(), frameNameLength), 0, frameNameLength);
				for (int x = 0; x < getSide(); x++) {
					for (int y = 0; y < getSide(); y++) {
						for (int z = 0; z < getSide(); z++) {
							frameData[(int) ((z * Math.pow(getSide(), 2) * 3) + (y * getSide() * 3) + (x * 3) + 0)] = Integer.valueOf((int) (Math.round(getFrame(i).getVoxel(x, y, z).getColor().getRed() * 255))).byteValue();
							frameData[(int) ((z * Math.pow(getSide(), 2) * 3) + (y * getSide() * 3) + (x * 3) + 1)] = Integer.valueOf((int) (Math.round(getFrame(i).getVoxel(x, y, z).getColor().getGreen() * 255))).byteValue();
							frameData[(int) ((z * Math.pow(getSide(), 2) * 3) + (y * getSide() * 3) + (x * 3) + 2)] = Integer.valueOf((int) (Math.round(getFrame(i).getVoxel(x, y, z).getColor().getBlue() * 255))).byteValue();
						}
					}
				}
				outputStream.write(frameData, 0, frameData.length);
			}

			outputStream.closeEntry();
			outputStream.close();

			this.filePath = path;
			modified = false;
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static Project loadProject(Editor editor, String path) {
		try {
			// load file and check signature
			FileInputStream fis = new FileInputStream(path);
			ZipInputStream inputStream = new ZipInputStream(fis);
			inputStream.getNextEntry();

			byte[] fileSignature = new byte[QU3D_SIGNATURE.length];
			if(inputStream.read(fileSignature) < 0)
				return null;
			for (int i = 0; i < fileSignature.length; i++)
				if (fileSignature[i] != QU3D_SIGNATURE[i])
					return null;
			// load remaining part of file data
			byte[] fileCubeData = new byte[1];
			if(inputStream.read(fileCubeData) < 0)
				return null;
			int fileCubeSide = fileCubeData[0] & 0xFF;

			byte[] fileFrameData = new byte[4];
			if(inputStream.read(fileFrameData) < 0)
				return null;
			int fileFrameSize = ByteBuffer.wrap(fileFrameData).getInt();
			// run file data validation
			if (fileCubeSide > MAX_SIDE || fileFrameSize < 0 || fileFrameSize > MAX_FRAME_COUNT)
				return null;
			// load option data
			byte[] optionData = new byte[5];
			if(inputStream.read(optionData) < 0)
				return null;
			boolean fileLoop = (optionData[0] & 0xFF) == 255;
			int filePaintColorRed = optionData[1] & 0xFF;
			int filePaintColorGreen = optionData[2] & 0xFF;
			int filePaintColorBlue = optionData[3] & 0xFF;
			int filePaintColorOpacity = optionData[4] & 0xFF;

			byte[] lastlyEditedFrameData = new byte[4];
			if(inputStream.read(lastlyEditedFrameData) < 0)
				return null;
			int lastlyEditedFrame = ByteBuffer.wrap(lastlyEditedFrameData).getInt();

			// set up project
			Project retProject = new Project(editor, new File(path).getName().split("\\.")[0], fileCubeSide);
			retProject.setLoop(fileLoop);
			retProject.setPaintColor(new Color((double) filePaintColorRed / 255, (double) filePaintColorGreen / 255, (double) filePaintColorBlue / 255, (double) filePaintColorOpacity / 255));

			// load all frames
			byte[] frameDurationData = new byte[BYTES_IN_FRAME_DURATION];
			byte[] frameNameLengthByte = new byte[1];
			byte[] frameVoxelData = new byte[(int) (3 * Math.pow(fileCubeSide, 3))];
			for (int i = 0; i < fileFrameSize; i++) {
				if (i > 0)	// add new frames as they are iterated through
					retProject.addFrame(null, i);
				// load duration
				if(inputStream.read(frameDurationData) < 0)
					return null;
				retProject.getFrame(i).setDurationInMillis(ByteBuffer.wrap(frameDurationData).getFloat());
				// load frame name
				if(inputStream.read(frameNameLengthByte) < 0)
					return null;
				int frameNameLength = frameNameLengthByte[0] & 0xFF;
				byte[] frameNameArray= new byte[frameNameLength];
				if(inputStream.read(frameNameArray) < 0)
					return null;
				retProject.getFrame(i).setName(GeneralMethods.byteArrayToString(frameNameArray));
				// load frame voxel colors
				if(inputStream.read(frameVoxelData) < 0)
					return null;
				for (int x = 0; x < fileCubeSide; x++) {
					for (int y = 0; y < fileCubeSide; y++) {
						for (int z = 0; z < fileCubeSide; z++) {
							int voxelColorRed = frameVoxelData[(int) ((z * Math.pow(fileCubeSide, 2) * 3) + (y * fileCubeSide * 3) + (x * 3) + 0)] & 0xFF;
							int voxelColorGreen = frameVoxelData[(int) ((z * Math.pow(fileCubeSide, 2) * 3) + (y * fileCubeSide * 3) + (x * 3) + 1)] & 0xFF;
							int voxelColorBlue = frameVoxelData[(int) ((z * Math.pow(fileCubeSide, 2) * 3) + (y * fileCubeSide * 3) + (x * 3) + 2)] & 0xFF;
							retProject.getFrame(i).getVoxel(x, y, z).setColor(new Color((double) voxelColorRed / 255, (double) voxelColorGreen / 255, (double) voxelColorBlue / 255, 1.0d));
						}
					}
				}
			}

			inputStream.closeEntry();
			inputStream.close();

			retProject.setCurrentFrame(lastlyEditedFrame);
			retProject.setFilePath(path);
			retProject.setModified(false);

			return retProject;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
