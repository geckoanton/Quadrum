/*
 * Animation.java
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

package project;

import javafx.application.Platform;
import serial.SerialInterface;
import ui.editor.controls.simulator.SimulatorControls;

public class Animation implements Runnable {
	private static final long MIN_SLEEP_TIME = 17,
			START_FRAME_DELAY = 100;

	private Project project;
	private Thread thread = new Thread(this);

	private SimulatorControls simulatorControls;
	private SerialInterface serialInterface;

	public Animation(Project project, SerialInterface serialInterface) {
		this.project = project;
		this.serialInterface = serialInterface;
	}

	public void startAnimation(SimulatorControls simulatorControls) {
		terminateAnimation();
		if(project == null)
			return;
		this.simulatorControls = simulatorControls;
		thread = new Thread(this);
		thread.start();
		serialInterface.setToggleZeroFrame(true);
	}
	public void terminateAnimation() {
		if(thread == null) {
			serialInterface.sendZeroFrame(project.getSide());
			return;
		}
		thread.interrupt();
		thread = null;

		if(simulatorControls == null)
			return;
		simulatorControls.setFrameOverride(-1);
		simulatorControls.updateLayout();
	}

	@Override
	public void run() {
		serialInterface.startStreamingProject(project);
		serialInterface.sendFrame(project.getFrame(0));
		try{ Thread.sleep(START_FRAME_DELAY); }
		catch(InterruptedException e) {}

		do {
			boolean terminated = false;
			for(int i = 0; i < project.getFrameSize(); i++) {
				long timeBefore = System.currentTimeMillis();

				simulatorControls.setFrameOverride(i);

				serialInterface.displayStreamedFrame();
				serialInterface.sendFrame(project.getFrame((i + 1) % project.getFrameSize()));

				Platform.runLater(() -> simulatorControls.updateLayout());	// must run on platform thread to avoid error when drawing to canvas

				long timeAfter = System.currentTimeMillis();
				long sleepTime = ((long) project.getFrame(i).getDurationInMillis()) - (timeAfter - timeBefore);
				sleepTime = Math.max(sleepTime, MIN_SLEEP_TIME);

				try{ Thread.sleep(sleepTime); }
				catch(InterruptedException e) {
					terminated = true;
					break;
				}
			}
			if(terminated)
				break;
		}
		while(project.isLoop());

		serialInterface.sendZeroFrame(project.getSide());

		serialInterface.endStreamingProject(project);
		Platform.runLater(this::terminateAnimation);
	}
}
