/*
 * CodeGeneratorControls.java
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

package ui.editor.controls;

import codeGenerator.CodeGenerator;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import main.ProjectManager;
import ui.editor.EditorController;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CodeGeneratorControls implements ProjectControls {
	private ProjectManager projectManager;

	private AnchorPane codeGeneratorAnchorPane;
	private Text codeGeneratorText;
	private ImageView codeGeneratorImageView;

	private TextArea codeGeneratorTextArea;

	private ComboBox<String> colorChannelsComboBox, channelResolutionComboBox;
	
	public CodeGeneratorControls(ProjectManager projectManager,
								 AnchorPane codeGeneratorAnchorPane, Text codeGeneratorText, ImageView codeGeneratorImageView,
								 TextArea codeGeneratorTextArea,
								 ComboBox<String> colorChannelsComboBox, ComboBox<String> channelResolutionComboBox) {
		this.projectManager = projectManager;

		this.codeGeneratorAnchorPane = codeGeneratorAnchorPane;
		this.codeGeneratorText = codeGeneratorText;
		this.codeGeneratorImageView = codeGeneratorImageView;

		this.codeGeneratorTextArea = codeGeneratorTextArea;

		this.colorChannelsComboBox = colorChannelsComboBox;
		this.channelResolutionComboBox = channelResolutionComboBox;

		codeGeneratorTextArea.setWrapText(true);

		colorChannelsComboBox.setItems(FXCollections.observableArrayList("Single (One Color)", "Double (R and G)", "Triple (Full RGB)"));
		colorChannelsComboBox.getSelectionModel().select(0);

		channelResolutionComboBox.setItems(FXCollections.observableArrayList("1 bit (ON or OFF)", "2 bit (4 steps)", "4 bit (16 steps)", "8 bit (256 steps)"));
		channelResolutionComboBox.getSelectionModel().select(0);
	}

	@Override
	public void updateProject() {
		setNullProject(projectManager.getCurrent() == null);
	}
	@Override
	public void updateLayout() {}
	private void setNullProject(boolean nullProject) {
		float opacity = 1.0f;
		if(nullProject) {
			opacity = EditorController.DISABLED_OPACITY;
		}

		codeGeneratorImageView.setOpacity(opacity);
		codeGeneratorText.setOpacity(opacity);

		codeGeneratorAnchorPane.setDisable(nullProject);
	}

	public void generateCode() {
		if(projectManager.getCurrent() == null)
			return;

		int channelCount = colorChannelsComboBox.getSelectionModel().getSelectedIndex() + 1;
		int channelSize = (int) Math.pow(2, channelResolutionComboBox.getSelectionModel().getSelectedIndex());

		codeGeneratorTextArea.setText(CodeGenerator.getCodeString(projectManager.getCurrent(), channelCount, channelSize));
	}
	public void copyCode() {
		StringSelection selection = new StringSelection(codeGeneratorTextArea.getText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}
}
