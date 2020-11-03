/*
 * WelcomePageController.java
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

package ui.editor.welcomePage;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import main.Editor;
import main.GeneralMethods;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;
import ui.editor.controls.MenuControls;

public class WelcomePageController {
	// fxml
	@FXML
	private WebView welcomeWebView;

	public void initialize() {
		WebEngine engine = welcomeWebView.getEngine();

		// open urls in web view window in the default web browser
		engine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				NodeList nodeList = welcomeWebView.getEngine().getDocument().getElementsByTagName("a");
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					EventTarget eventTarget = (EventTarget) node;
					eventTarget.addEventListener("click", new EventListener() {
						@Override
						public void handleEvent(Event evt) {
							EventTarget target = evt.getCurrentTarget();
							HTMLAnchorElement anchorElement = (HTMLAnchorElement) target;
							String href = anchorElement.getHref();

							GeneralMethods.openUrl(href);

							System.out.println(href);
							evt.preventDefault();
						}
					}, false);
				}
			}
		});

		engine.load(Editor.WELCOME_PAGE_URL);
	}

	@FXML
	private void newProjectAction() {
		if(menuControls == null)
			return;
		closeWelcomePage();
		menuControls.newProjectAction();
	}
	@FXML
	private void openProjectAction() {
		if(menuControls == null)
			return;
		closeWelcomePage();
		menuControls.openProjectAction();
	}
	@FXML
	private void closeWelcomePageAction() {
		closeWelcomePage();
	}

	// non-fxml

	private Scene scene = null;
	private Parent editorRoot = null;

	private MenuControls menuControls = null;

	public void initialize(Scene editorScene, Parent editorRoot,
						   MenuControls menuControls) {
		this.scene = editorScene;
		this.editorRoot = editorRoot;

		this.menuControls = menuControls;
	}

	private void closeWelcomePage() {
		if(scene == null || editorRoot == null)
			return;
		scene.setRoot(editorRoot);
	}

}
