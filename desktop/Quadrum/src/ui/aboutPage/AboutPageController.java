/*
 * AboutPageController.java
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

package ui.aboutPage;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import main.Editor;
import main.GeneralMethods;

public class AboutPageController {
	// fxml related

	@FXML
	public void okAction() {
		if(aboutStage == null)
			return;
		aboutStage.close();
	}
	@FXML
	public void geckoantonHyperLinkAction() {
		GeneralMethods.openUrl(Editor.geckoanton_URL);
	}
	public void icons8HyperLinkAction() {
		GeneralMethods.openUrl(Editor.ICONS8_URL);
	}
	public void serialLibraryHyperLinkAction() {
		GeneralMethods.openUrl(Editor.SERIAL_LIBRARY_URL);
	}
	public void quadrumHyperLinkAction() {
		GeneralMethods.openUrl(Editor.QUADRUM_URL);
	}
	public void contactHyperlinkAction() {
		GeneralMethods.openUrl(Editor.CONTACT_ME_URL);
	}

	// non-fxml

	private Stage aboutStage;

	protected void initialize(Stage aboutStage) { this.aboutStage = aboutStage; }
}
