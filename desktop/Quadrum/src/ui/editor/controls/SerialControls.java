/*
 * SerialControls.java
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

import javafx.application.Platform;
import javafx.scene.control.*;
import main.ProjectManager;
import serial.PortScanner;
import serial.SerialInterface;

public class SerialControls {
	private SerialInterface serialInterface;

	private ListView<String> availablePortsListView;
	private Button connectSerialButton, disconnectSerialButton;
	private RadioButton customPortRadioButton;
	private TextField customPortTextField;
	private TextArea consoleTextArea;

	public SerialControls(SerialInterface serialInterface, PortScanner portScanner,
						  ListView<String> availablePortsListView, RadioButton customPortRadioButton, TextField customPortTextField,
						  Button connectSerialButton, Button disconnectSerialButton,
						  TextArea consoleTextArea) {
		this.serialInterface = serialInterface;

		this.availablePortsListView = availablePortsListView;
		this.customPortRadioButton = customPortRadioButton;
		this.customPortTextField = customPortTextField;

		this.connectSerialButton = connectSerialButton;
		this.disconnectSerialButton = disconnectSerialButton;

		this.consoleTextArea = consoleTextArea;

		connectedMode(serialInterface.isConnected());
		portScanner.startScan(serialInterface, this);

		consoleTextArea.setWrapText(true);
	}

	public void connectAction() {
		if(customPortRadioButton.isSelected())
			serialInterface.setSerialPort(customPortTextField.getText());
		serialInterface.connect(this);
	}
	public void disconnectAction() {
		serialInterface.disconnect();
	}
	
	public synchronized void printlnConsole(String printValue) {
		Platform.runLater(() -> consoleTextArea.setText(consoleTextArea.getText() + printValue + "\n"));
		Platform.runLater(() -> consoleTextArea.positionCaret(consoleTextArea.getText().length()));
	}
	public synchronized void clearConsole() {
		Platform.runLater(() -> consoleTextArea.setText(""));
	}
	public synchronized void connectedMode(boolean mode) {
		Platform.runLater(() -> connectSerialButton.setDisable(mode));
		Platform.runLater(() -> disconnectSerialButton.setDisable(!mode));
	}

	public void updateListViewPort() {
		if(availablePortsListView.getItems().size() > 0) {
			customPortRadioButton.setSelected(false);
			serialInterface.setSerialPort(availablePortsListView.getSelectionModel().getSelectedItem());
		}
	}
	public void updateCustomPort() {
		if(customPortRadioButton.isSelected()) {
			availablePortsListView.getSelectionModel().clearSelection();
			serialInterface.setSerialPort(customPortTextField.getText());
		}
	}
	public synchronized void updateAvailablePorts(String[] ports) {
		for(int i = 0; i < availablePortsListView.getItems().size() || i < ports.length; i++) {
			if(i < ports.length) {
				if (i < availablePortsListView.getItems().size())
					availablePortsListView.getItems().set(i, ports[i]);
				else
					availablePortsListView.getItems().add(ports[i]);

				if(availablePortsListView.getItems().get(i).equals(serialInterface.getSerialPort()))
					availablePortsListView.getSelectionModel().select(i);
			}
			else
				availablePortsListView.getItems().remove(i);
		}
	}
}
