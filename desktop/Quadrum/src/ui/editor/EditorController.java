/*
 * EditorController.java
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

package ui.editor;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import main.ProjectManager;
import serial.PortScanner;
import serial.SerialInterface;
import ui.editor.controls.CodeGeneratorControls;
import ui.editor.controls.MenuControls;
import ui.editor.controls.ProjectControls;
import ui.editor.controls.SerialControls;
import ui.editor.controls.paint.FrameControls;
import ui.editor.controls.paint.PaintControls;
import ui.editor.controls.simulator.SimulatorControls;
import java.util.ArrayList;

public class EditorController {
	// fxml related
	public static final float DISABLED_OPACITY = 0.35f;

	@FXML
	private AnchorPane mainAnchorPane;
	@FXML
	private MenuBar mainMenuBar;

	public void initialize() {
		final String os = System.getProperty("os.name");
		if (os != null && os.startsWith("Mac")) {
			mainMenuBar.useSystemMenuBarProperty().set(true);
		}

		// lock paintPanelCanvas size to paintPanelPane size
		paintPanelPane.widthProperty().addListener(e -> updatePaintPanelCanvasSize());
		paintPanelPane.heightProperty().addListener(e -> updatePaintPanelCanvasSize());

		// lock simulatorPanelCanvas size to simulatorPanelPane size
		simulatorPanelPane.widthProperty().addListener(e -> updateSimulatorPanelCanvas());
		simulatorPanelPane.heightProperty().addListener(e -> updateSimulatorPanelCanvas());

		// frameDurationSpinner
		frameDurationSpinner.valueProperty().addListener(this::frameDurationSpinnerAction);
		frameDurationSpinner.getEditor().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> frameDurationSpinner.getEditor().selectAll());	// select all text on click
		frameDurationSpinner.getValueFactory().setConverter(new StringConverter<>() {	// disable error print when user inputs illegal arguments
			public String toString(Double value) {
				if (value == null)
					return null;
				if (value.floatValue() % 1 != 0)
					return value.toString();
				else
					return Integer.toString(value.intValue());
			}
			public Double fromString(String string) {
				try {
					return Double.parseDouble(string);
				}
				catch (Exception e) {
					return null;
				}
			}
		});

		// frameListView
		frameListView.setEditable(true);
		frameListView.setCellFactory(TextFieldListCell.forListView());	// makes frameListView editable, with changeable frame name
		frameListView.setOnEditCommit(e -> frameListViewEditCommit(e.getNewValue()));

		// projectTabPane
		projectTabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> projectTabPaneAction());

		// tab icons
		ImageView paintTabIcon = new ImageView("res/images/icons/paintTiltedIcon.png");
		paintTabIcon.setFitWidth(26);
		paintTabIcon.setFitHeight(26);
		paintTab.setGraphic(paintTabIcon);

		ImageView serialTabIcon = new ImageView("res/images/icons/serialTiltedIcon.png");
		serialTabIcon.setFitWidth(26);
		serialTabIcon.setFitHeight(26);
		serialTab.setGraphic(serialTabIcon);

		ImageView codeTabIcon = new ImageView("res/images/icons/codeTiltedIcon.png");
		codeTabIcon.setFitWidth(26);
		codeTabIcon.setFitHeight(26);
		codeTab.setGraphic(codeTabIcon);
	}
	private void updatePaintPanelCanvasSize() {
		paintPanelCanvas.setWidth(paintPanelPane.getWidth());
		paintPanelCanvas.setHeight(paintPanelPane.getHeight());
		if(paintControls != null)
			paintControls.updateLayout();
	}
	private void updateSimulatorPanelCanvas() {
		simulatorPanelCanvas.setWidth(simulatorPanelPane.getWidth());
		simulatorPanelCanvas.setHeight(simulatorPanelPane.getHeight());
		if(simulatorControls != null)
			simulatorControls.updateLayout();
	}

	// paint panel

	@FXML
	private Canvas paintPanelCanvas;
	@FXML
	private Pane paintPanelPane;
	@FXML
	private Button previousPageButton;
	@FXML
	private Button nextPageButton;
	@FXML
	private ColorPicker paintColorPicker;

	@FXML
	private void previousPageAction() {
		if(paintControls == null)
			return;
		paintControls.previousPage();
	}
	@FXML
	private void paintColorPickerAction() {
		if(paintControls == null)
			return;
		paintControls.updatePaintColor();
	}
	@FXML
	private void nextPageAction() {
		if(paintControls == null)
			return;
		paintControls.nextPage();
	}

	// simulator panel

	@FXML
	private Canvas simulatorPanelCanvas;
	@FXML
	private Pane simulatorPanelPane;
	@FXML
	private Button playAnimationButton;
	@FXML
	private Button stopAnimationButton;
	@FXML
	private Text frameNameText;

	@FXML
	private void playAnimationAction() {
		if(simulatorControls == null)
			return;
		simulatorControls.playAnimation();
	}
	@FXML
	private void stopAnimationAction() {
		if(simulatorControls == null)
			return;
		simulatorControls.stopAnimation();
	}

	// frame panel

	@FXML
	private Text timeDurationText;
	@FXML
	private ImageView timeDurationImageView;
	@FXML
	private Spinner<Double> frameDurationSpinner;
	@FXML
	private Text msDurationText;
	@FXML
	private CheckBox loopCheckBox;
	@FXML
	private ListView<String> frameListView;
	@FXML
	private Button addFrameButton;
	@FXML
	private Button removeFrameButton;
	@FXML
	private Button copyFrameButton;
	@FXML
	private Button pasteFrameButton;

	@FXML
	private void frameDurationSpinnerAction(ObservableValue<?> observableValue, Double oldValue, Double newValue) {
		if(frameControls == null)
			return;
		frameControls.updateFrameDuration(oldValue.floatValue(), newValue.floatValue());
	}
	@FXML
	private void loopCheckBoxAction() {
		if(frameControls == null)
			return;
		frameControls.setLoop(loopCheckBox.isSelected());
	}
	@FXML
	public void frameListViewPressedAction() {
		if(frameControls == null)
			return;
		frameControls.updateCurrentFrame(frameListView.getSelectionModel().getSelectedIndex());
	}
	@FXML
	private void addFrameAction() {
		if(frameControls == null)
			return;
		frameControls.addFrame();
	}
	@FXML
	private void removeFrameAction() {
		if(frameControls == null)
			return;
		frameControls.removeFrame();
	}
	@FXML
	private void copyFrameAction() {
		if(frameControls == null)
			return;
		frameControls.copyFrame();
	}
	@FXML
	private void pasteFrameAction() {
		if(frameControls == null)
			return;
		frameControls.pasteFrame();
	}
	@FXML
	private void frameListViewEditStart() {
		if(frameControls == null)
			return;
		frameControls.setFrameListViewEdit(frameListView.getEditingIndex());
	}
	@FXML
	private void frameListViewEditCommit(String value) {
		if(frameControls == null)
			return;
		frameControls.setFrameName(value);
	}

	// menu bar and projects tab

	@FXML
	private MenuItem saveMenuItem;
	@FXML
	private MenuItem undoMenuItem;
	@FXML
	private MenuItem redoMenuItem;
	@FXML
	private MenuItem copyFrameMenuItem;
	@FXML
	private MenuItem pasteFrameMenuItem;
	@FXML
	private MenuItem playMenuItem;
	@FXML
	private MenuItem stopMenuItem;
	@FXML
	private CheckMenuItem gridCheckMenuItem;

	@FXML
	private TabPane projectTabPane;
	@FXML
	private TabPane serialPaintTabPane;

	@FXML
	private Tab paintTab;
	@FXML
	private Tab serialTab;
	@FXML
	private Tab codeTab;

	@FXML
	private void newProjectAction() {
		if(menuControls == null)
			return;
		menuControls.newProjectAction();
	}
	@FXML
	private void openProjectAction() {
		if(menuControls == null)
			return;
		menuControls.openProjectAction();
	}
	@FXML
	private void saveProjectAction() {
		if(menuControls == null)
			return;
		menuControls.saveProjectAction();
	}
	@FXML
	private void undoAction() {
		if(menuControls == null)
			return;
		menuControls.undoAction();
	}
	@FXML
	private void redoAction() {
		if(menuControls == null)
			return;
		menuControls.redoAction();
	}
	@FXML
	private void gridAction() {
		if(menuControls == null)
			return;
		menuControls.setGrid(gridCheckMenuItem.isSelected());
	}
	@FXML
	private void helpAction() {
		if(menuControls == null)
			return;
		menuControls.helpAction();
	}
	@FXML
	private void aboutAction() {
		if(menuControls == null)
			return;
		menuControls.aboutAction();
	}
	private void projectTabPaneAction() {
		if(menuControls == null)
			return;
		menuControls.projectTabPaneAction();
	}

	// serial panel

	@FXML
	private ListView<String> availablePortsListView;
	@FXML
	private RadioButton customPortRadioButton;
	@FXML
	private TextField customPortTextField;
	@FXML
	private Button connectSerialButton;
	@FXML
	private Button disconnectSerialButton;
	@FXML
	private TextArea consoleTextArea;

	@FXML
	private void connectSerialAction() {
		if(serialControls == null)
			return;
		serialControls.connectAction();;
	}
	@FXML
	private void disconnectSerialAction() {
		if(serialControls == null)
			return;
		serialControls.disconnectAction();
	}
	@FXML
	private void availablePortsListViewPressedAction() {
		if(serialControls == null)
			return;
		serialControls.updateListViewPort();
	}
	@FXML
	private void customPortAction() {
		if(serialControls == null)
			return;
		serialControls.updateCustomPort();
	}

	// code generator panel

	@FXML
	private AnchorPane codeGeneratorAnchorPane;
	@FXML
	private Text codeGeneratorText;
	@FXML
	private ImageView codeGeneratorImageView;
	@FXML
	private TextArea codeGeneratorTextArea;
	@FXML
	private ComboBox<String> colorChannelsComboBox;
	@FXML
	private ComboBox<String> channelResolutionComboBox;

	@FXML
	private void generateCodeAction() {
		if(codeGeneratorControls == null)
			return;
		codeGeneratorControls.generateCode();
	}
	@FXML
	private void copyCodeAction() {
		if(codeGeneratorControls == null)
			return;
		codeGeneratorControls.copyCode();
	}

	// non-fxml

	private ArrayList<ProjectControls> projectControls = new ArrayList<>();

	private PaintControls paintControls = null;
	private SimulatorControls simulatorControls = null;
	private FrameControls frameControls = null;
	public MenuControls menuControls = null;	// gets sent to welcomePageController, therefore public

	private CodeGeneratorControls codeGeneratorControls = null;

	private SerialControls serialControls = null;

	public void initialize(ProjectManager projectManager, Stage stage, SerialInterface serialInterface, PortScanner portScanner, Clipboard clipboard) {
		// paintControls
		paintControls = new PaintControls(projectManager, paintPanelCanvas,
				previousPageButton, nextPageButton, paintColorPicker);
		projectControls.add(paintControls);
		// add mouse actions to paintControls
		mainAnchorPane.addEventFilter(MouseEvent.MOUSE_MOVED, e -> paintControls.mouseMoved(	// give mouse moved position relative to anchor pane
				(float) (e.getSceneX() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinY())));
		paintPanelPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> paintControls.mousePressed(	// give mouse pressed position relative to anchor pane
				(float) (e.getSceneX() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinY())));
		paintPanelPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> paintControls.mouseDragged(	// give mouse dragged position relative to anchor pane
				(float) (e.getSceneX() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinY())));
		paintPanelPane.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> paintControls.mouseReleased(	// give mouse released position relative to anchor pane
				(float) (e.getSceneX() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - paintPanelPane.localToScene(paintPanelPane.getBoundsInLocal()).getMinY())));

		// simulatorControls
		simulatorControls = new SimulatorControls(projectManager, simulatorPanelCanvas,
				playAnimationButton, stopAnimationButton, frameNameText,
				playMenuItem, stopMenuItem);
		projectControls.add(simulatorControls);
		// add mouse actions to simulatorControls
		simulatorPanelPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> simulatorControls.mousePressed(	// give mouse pressed position relative to anchor pane
				(float) (e.getSceneX() - simulatorPanelPane.localToScene(simulatorPanelPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - simulatorPanelPane.localToScene(simulatorPanelPane.getBoundsInLocal()).getMinY())));
		simulatorPanelPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> simulatorControls.mouseDragged(	// give mouse dragged position relative to anchor pane
				(float) (e.getSceneX() - simulatorPanelPane.localToScene(simulatorPanelPane.getBoundsInLocal()).getMinX()),
				(float) (e.getSceneY() - simulatorPanelPane.localToScene(simulatorPanelPane.getBoundsInLocal()).getMinY())));

		// frameControls
		frameControls = new FrameControls(projectManager, clipboard,
				timeDurationText, msDurationText, timeDurationImageView, frameDurationSpinner, frameListView, loopCheckBox,
				addFrameButton, removeFrameButton, copyFrameButton, copyFrameMenuItem, pasteFrameButton, pasteFrameMenuItem);
		projectControls.add(frameControls);

		// menuControls
		menuControls = new MenuControls(projectManager, stage,
				saveMenuItem,
				undoMenuItem, redoMenuItem,
				gridCheckMenuItem,
				projectTabPane, serialPaintTabPane);
		projectControls.add(menuControls);

		// code generator controls
		codeGeneratorControls = new CodeGeneratorControls(projectManager,
				codeGeneratorAnchorPane, codeGeneratorText, codeGeneratorImageView,
				codeGeneratorTextArea,
				colorChannelsComboBox, channelResolutionComboBox);
		projectControls.add(codeGeneratorControls);

		// serial controls
		serialControls = new SerialControls(serialInterface, portScanner,
				availablePortsListView, customPortRadioButton, customPortTextField,
				connectSerialButton, disconnectSerialButton,
				consoleTextArea);
	}

	public void updateProject() {
		for(ProjectControls pj : projectControls)
			pj.updateProject();
	}
	public void updateLayout() {
		for(ProjectControls pj : projectControls)
			pj.updateLayout();
	}

	public boolean onClose() {
		if(menuControls == null)
			return false;
		return menuControls.closeAllProjects();
	}
}
