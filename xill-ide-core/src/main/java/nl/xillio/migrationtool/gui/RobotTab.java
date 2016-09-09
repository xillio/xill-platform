/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.migrationtool.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.SaveBeforeClosingDialog;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient;
import nl.xillio.migrationtool.gui.EditorPane.DocumentState;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.Robot;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.util.settings.Settings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * A tab containing the editor, console and debug panel attached to a specific currentRobot.
 */
public class RobotTab extends FileTab implements Initializable {
    private static final Logger LOGGER = Log.get();
    // The event used to silently close a tab.
    public static final EventType<Event> SILENT_CLOSED_EVENT = new EventType<>(Event.ANY, "TAB_CLOSED_SILENT");

    // Status icons.
    private static final String PATH_STATUSICON_RUNNING = "M256,92.481c44.433,0,86.18,17.068,117.553,48.064C404.794,171.411,422,212.413,422,255.999 s-17.206,84.588-48.448,115.455c-31.372,30.994-73.12,48.064-117.552,48.064s-86.179-17.07-117.552-48.064 C107.206,340.587,90,299.585,90,255.999s17.206-84.588,48.448-115.453C169.821,109.55,211.568,92.481,256,92.481 M256,52.481 c-113.771,0-206,91.117-206,203.518c0,112.398,92.229,203.52,206,203.52c113.772,0,206-91.121,206-203.52 C462,143.599,369.772,52.481,256,52.481L256,52.481z M206.544,357.161V159.833l160.919,98.666L206.544,357.161z";
    private static final String PATH_STATUSICON_PAUSED = "M256,92.481c44.433,0,86.18,17.068,117.553,48.064C404.794,171.411,422,212.413,422,255.999 s-17.206,84.588-48.448,115.455c-31.372,30.994-73.12,48.064-117.552,48.064s-86.179-17.07-117.552-48.064 C107.206,340.587,90,299.585,90,255.999s17.206-84.588,48.448-115.453C169.821,109.55,211.568,92.481,256,92.481 M256,52.481 c-113.771,0-206,91.117-206,203.518c0,112.398,92.229,203.52,206,203.52c113.772,0,206-91.121,206-203.52 C462,143.599,369.772,52.481,256,52.481L256,52.481z M240.258,346h-52.428V166h52.428V346z M326.17,346h-52.428V166h52.428V346z";
    private final Group statusIconRunning = createIcon(PATH_STATUSICON_RUNNING);
    private final Group statusIconPaused = createIcon(PATH_STATUSICON_PAUSED);

    @FXML
    private HBox hbxBot;
    @FXML
    private SplitPane spnBotPanes;
    @FXML
    private SplitPane spnBotLeft;
    @FXML
    private VBox vbxDebugHidden;
    @FXML
    private VBox vbxDebugpane;
    @FXML
    private ConsolePane consolePane;
    @FXML
    private StatusBar apnStatusBar;

    private XillProcessor processor;

    private RobotID currentRobot;
    private LinkedList<RobotTab> relatedHighlightTabs = new LinkedList<>(); // This list contains the tabs that have been highlighted from this tab (e.g. compile error of included bot)

    /**
     * Create a new RobotTab that holds a robot
     *
     * @param projectPath      The project path
     * @param documentPath     The full path to the currentRobot (absolute)
     * @param globalController The FXController
     */
    public RobotTab(final File projectPath, final File documentPath, final FXController globalController) {
        super("/fxml/RobotTabContent.fxml", projectPath, documentPath, globalController);

        // Load the processor.
        try {
            processor = Loader.getXill().buildProcessor(projectPath.toPath(), documentPath.toPath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        currentRobot = getProcessor().getRobotID();

        // Initialize the settings and tab.
        initializeSettings(documentPath);
        initializeTab(documentPath);

        // Validate the robot as soon as the editor is loaded.
        editorPane.getEditor().getOnDocumentLoaded().addListener(e -> validate());
    }

    private static void initializeSettings(final File documentPath) {
        settings.simple().register(Settings.LAYOUT, Settings.RIGHT_PANEL_WIDTH + documentPath.getAbsolutePath(), "0.7", "Width of the right panel for the specified currentRobot");
        settings.simple().register(Settings.LAYOUT, Settings.RIGHT_PANEL_COLLAPSED + documentPath.getAbsolutePath(), "true", "The collapsed-state of the right panel for the specified currentRobot");
        settings.simple().register(Settings.LAYOUT, Settings.EDITOR_HEIGHT + documentPath.getAbsolutePath(), "0.6", "The height of the editor");
    }

    private void initializeTab(final File documentPath) {
        // Set the tab dividers
        double editorHeight = Double.parseDouble(settings.simple().get(Settings.LAYOUT, Settings.EDITOR_HEIGHT + documentPath.getAbsolutePath()));

        spnBotLeft.setDividerPosition(0, editorHeight);

        // Save the position on change
        spnBotLeft.getDividers().get(0).positionProperty().addListener(
                (observable, oldPos, newPos) -> {
                    double height = newPos.doubleValue();
                    settings.simple().save(Settings.LAYOUT, Settings.EDITOR_HEIGHT + documentPath.getAbsolutePath(), Double.toString(height));
                });

        // Status icons
        statusIconRunning.setAutoSizeChildren(true);
        statusIconPaused.setAutoSizeChildren(true);

        // Subscribe to start/stop for icon change
        processor.getDebugger().getOnRobotStart().addListener(e -> Platform.runLater(() -> setGraphic(statusIconRunning)));
        processor.getDebugger().getOnRobotStop().addListener(e -> Platform.runLater(() -> setGraphic(null)));
        processor.getDebugger().getOnRobotPause().addListener(e -> Platform.runLater(() -> setGraphic(statusIconPaused)));
        processor.getDebugger().getOnRobotContinue().addListener(e -> Platform.runLater(() -> setGraphic(statusIconRunning)));
        apnStatusBar.registerDebugger(processor.getDebugger());
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resources) {


        Platform.runLater(() -> {
            getEditorPane().initialize(this);
            setText(getName());
            if (documentPath.exists()) {
                try {
                    String code = FileUtils.readFileToString(documentPath);
                    editorPane.setLastSavedCode(code);
                    editorPane.getEditor().setCode(code);
                } catch (IOException e) {
                    LOGGER.info("Could not open " + documentPath, e);
                }
            }

            // Subscribe to events
            editorPane.getDocumentState().addListener(this);
            consolePane.initialize(this);
            vbxDebugpane.getChildrenUnmodifiable().filtered(node -> node instanceof DebugPane).forEach(node -> ((DebugPane) node).initialize(this));

            // Remove the left hidden bar from dom
            // This must be done after initialization otherwise the debugpane won't receive the tab
            boolean showRightPanel = Boolean.parseBoolean(settings.simple().get(Settings.LAYOUT, Settings.RIGHT_PANEL_COLLAPSED + getDocument().getAbsolutePath()));

            if (showRightPanel) {
                hideButtonPressed();
            } else {
                showButtonPressed();
            }
        });
    }

    /**
     * Hide the debugpane
     */
    @FXML
    private void hideButtonPressed() {
        File document = processor.getRobotID().getPath();
        if (document != null) {
            settings.simple().save(Settings.LAYOUT, Settings.RIGHT_PANEL_COLLAPSED + document.getAbsolutePath(), "true");
            if (!spnBotPanes.getDividers().isEmpty()) {
                settings.simple().save(Settings.LAYOUT, Settings.RIGHT_PANEL_WIDTH + document.getAbsolutePath(), Double.toString(spnBotPanes.getDividerPositions()[0]));
            }
        }

        // Hide debug
        spnBotPanes.getItems().remove(vbxDebugpane);

        // Show the small bar
        if (!hbxBot.getChildren().contains(vbxDebugHidden)) {
            hbxBot.getChildren().add(vbxDebugHidden);
        }
    }

    /**
     * Show the debug pane
     */
    @FXML
    private void showButtonPressed() {
        File document = processor.getRobotID().getPath();
        settings.simple().save(Settings.LAYOUT, Settings.RIGHT_PANEL_COLLAPSED + document.getAbsolutePath(), "false");

        // Hide small bar
        hbxBot.getChildren().remove(vbxDebugHidden);

        // Show debugpane
        if (!spnBotPanes.getItems().contains(vbxDebugpane)) {
            spnBotPanes.getItems().add(vbxDebugpane);
        }

        // Add splitpane position listener
        spnBotPanes.setDividerPosition(0, Double.parseDouble(settings.simple().get(Settings.LAYOUT, Settings.RIGHT_PANEL_WIDTH + document.getAbsolutePath())));
        spnBotPanes.getDividers().get(0).positionProperty().addListener((position, oldPos, newPos) -> {
            if (spnBotPanes.getItems().contains(vbxDebugpane)) {
                settings.simple().save(Settings.LAYOUT, Settings.RIGHT_PANEL_WIDTH + document.getAbsolutePath(), newPos.toString());
            }
        });
    }

    /**
     * Save this document.
     *
     * @param showDialog whether a "Save as..." dialog should be shown
     * @return whether the document was saved successfully.
     */
    @Override
    protected boolean save(final boolean showDialog) {
        // Do not save the contents of other robots (i.e. included robots being debugged).
        if (currentRobot != processor.getRobotID()) {
            return false;
        }

        // Clear editor highlights except when paused.
        if (getStatusBar().statusProperty().get() != StatusBar.Status.PAUSED) {
            getEditorPane().getEditor().clearHighlight();
        }

        // Save.
        if (!super.save(showDialog)) {
            return false;
        }

        // Build the processor.
        try {
            processor = Loader.getXill().buildProcessor(projectPath.toPath(), documentPath.toPath(), processor.getDebugger());
        } catch (IOException e) {
            AlertDialog error = new AlertDialog(AlertType.ERROR, "Failed to save robot", "", e.getMessage());
            error.show();
            LOGGER.error("Failed to save robot", e);
        }
        currentRobot = processor.getRobotID();

        // Validate.
        validate();

        return true;
    }

    protected void validate() {
        List<Issue> issues = getProcessor().validate()
                .stream()
                .filter(issue -> issue.getRobot() == getCurrentRobot())
                .collect(Collectors.toList());

        getEditorPane().getEditor().annotate(issues);
    }

    /**
     * Create an icon from an SVG shape.
     *
     * @param shape the shape to create an icon from
     * @return a group containing the icon
     */
    private static Group createIcon(final String shape) {
        SVGPath path = new SVGPath();
        path.setFill(Color.DARKGRAY);
        path.setScaleX(0.04);
        path.setScaleY(0.04);
        path.setContent(shape);
        return new Group(path);
    }

    @Override
    protected void onClose(final Event event) {
        // Check if the robot is saved, else show the save before closing dialog.
        if (editorPane.getDocumentState().getValue() == DocumentState.CHANGED) {
            SaveBeforeClosingDialog dlg = new SaveBeforeClosingDialog(this, event);
            dlg.showAndWait();
            if (dlg.isCancelPressed()) {
                globalController.setCancelClose(true);
            } else {
                checkLastTab();
            }
        } else if (getEditorPane().getControls().robotRunning()) {
            boolean silent = event.getEventType() == SILENT_CLOSED_EVENT;

            // If the robot is running and it is not a silent event show the stop robot dialog.
            if (silent || showCloseTabDialog()) {
                // Stop the robot.
                getEditorPane().getControls().stop();
                checkLastTab();
            } else {
                // Consume the event to prevent the tab from closing.
                event.consume();
            }
        } else {
            checkLastTab(); // If not running and saved then just check if it is the last robot.
        }
    }

    private boolean showCloseTabDialog() {
        AlertDialog dialog = new AlertDialog(AlertType.WARNING, getName() + " is still running.", "",
                "Closing the tab will stop the robot. Do you want to close the tab?",
                new ButtonType("Close", ButtonBar.ButtonData.YES), ButtonType.CANCEL);
        return dialog.showAndWait().get().getButtonData() == ButtonBar.ButtonData.YES;
    }

    /**
     * Runs the currentRobot after the Ok-button has been pressed of the dialog that pops up.
     * If cancel is pressed, the robot is not run or saved
     * There is also a check box that can be ticked in order not to show the dialog again.
     * The dialog can be re-enabled in the Settings window.
     *
     * @throws XillParsingException
     */
    public void runRobot() throws XillParsingException {
        // Read the current setting in the configuration
        boolean autoSaveBotBeforeRun = Boolean.parseBoolean(settings.simple().get(Settings.SETTINGS_GENERAL, Settings.AUTO_SAVE_BOT_BEFORE_RUN));

        if (autoSaveBotBeforeRun) {
            // Check if the content is unsaved, show the confirmation dialog.
            if (editorPane.getDocumentState().getValue() == DocumentState.CHANGED) {
                Alert confirmationDialog = new Alert(AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Do you want to save and run the robot?");
                // This enables Xillio icon to be displayed in the upper left corner
                confirmationDialog.initOwner(editorPane.getScene().getWindow());

                // Compose the dialog pane
                DialogPane dp = new DialogPane();
                VBox checkBoxContainer = new VBox();

                Label l = new Label("The robot " + currentRobot.getPath().getName() + " needs to be saved before running. Do you want to continue?");
                CheckBox cb = new CheckBox("Don't ask me again.");
                cb.addEventHandler(ActionEvent.ACTION, event -> {
                    boolean currentSettingValue = Boolean.parseBoolean(settings.simple().get(Settings.SETTINGS_GENERAL, Settings.AUTO_SAVE_BOT_BEFORE_RUN));
                    settings.simple().save(Settings.SETTINGS_GENERAL, Settings.AUTO_SAVE_BOT_BEFORE_RUN, !currentSettingValue);
                });
                checkBoxContainer.getChildren().addAll(l, cb);

                dp.setContent(checkBoxContainer);
                // Add the dialog pane to the Alert/dialog
                confirmationDialog.setDialogPane(dp);
                // Make the dialog close by clicking the close button, inherit styling
                confirmationDialog.initModality(Modality.APPLICATION_MODAL);
                confirmationDialog.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Get the result from the confirmation dialog
                Optional<ButtonType> result = confirmationDialog.showAndWait();

                // Process the result
                if (result.get() == ButtonType.OK) {
                    autoSaveAndRunRobot();
                } else {
                    return;
                }
            } else {
                autoSaveAndRunRobot();
            }
        } else {
            // If false, just auto-save and run the robot without the confirmation dialog popping up
            autoSaveAndRunRobot();
        }
    }

    /**
     * Automatically saves robot and runs it if the save is successful
     */
    @SuppressWarnings("squid:S1166") // XillParsingException is handled correctly here
    private void autoSaveAndRunRobot() {
        save();

        if (FXController.settings.simple().getBoolean(Settings.SETTINGS_GENERAL, Settings.RUN_BOT_WITH_CLEAN_CONSOLE)) {
            ESConsoleClient.getInstance().clearLog(getProcessor().getRobotID().toString());
        }

        try {
            apnStatusBar.setStatus(StatusBar.Status.COMPILING);
            processor.compile();
        } catch (IOException e) {
            errorPopup(-1, e.getLocalizedMessage(), e.getClass().getSimpleName(), "Exception while compiling.");
            return;
        } catch (XillParsingException e) {
            handleXillParsingError(e);
            LOGGER.error(e.getMessage(), e);
            return;
        } finally {
            apnStatusBar.setStatus(StatusBar.Status.READY);
        }

        Robot robot = processor.getRobot();

        LOGGER.info(MarkerFactory.getMarker("ROBOT_START"), "Robot " + getProcessor().getRobotID().toString() + " (CSID:" + getProcessor().getRobot().getCompilerSerialId() + ") is about to start.");

        Thread robotThread = new Thread(() -> {
            try {
                robot.process(processor.getDebugger());
            } catch (Exception e) {
                LOGGER.error("Exception while processing", e);
                Platform.runLater(() -> {
                    Alert error = new Alert(AlertType.ERROR);
                    error.initModality(Modality.APPLICATION_MODAL);
                    error.setTitle(e.getClass().getSimpleName());
                    error.setContentText(e.getMessage() + "\n" + ExceptionUtils.getStackTrace(e));
                    error.setHeaderText("Exception while processing");
                    error.setResizable(true);
                    error.getDialogPane().setPrefWidth(1080);
                    error.show();
                });
            }
        });

        robotThread.setUncaughtExceptionHandler((thread, e) -> {
            // This error can occur if, e.g. deep recursion, is performed.
            if (e instanceof StackOverflowError) {
                handleStackOverFlowError(robot, e.getClass().getName());
            }
        });

        robotThread.start();

    }

    private void handleStackOverFlowError(Robot robot, String dialogTitle) {
        // Release the robot resources.
        try {
            robot.close();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        // Notify the user of the error that occurred.
        Platform.runLater(() -> {

            getEditorPane().getControls().stop();
            setGraphic(null);
            apnStatusBar.setStatus(StatusBar.Status.STOPPED);

            new AlertDialog(AlertType.ERROR, dialogTitle, "", "A stack overflow exception occurred.").show();
        });
    }

    /**
     * Show alert dialog and highlight the line with error in the correct robottab
     * There are 3 options - when the error is: 1. in current robottab, 2. in included which is already opened, 3. in included which is not currently open
     *
     * @param e caught XillParsingException
     */
    private void handleXillParsingError(final XillParsingException e) {
        if (currentRobot == e.getRobot()) {// Parse error in this robot
            errorPopup(e.getLine(), e.getLocalizedMessage(), e.getClass().getSimpleName(), "Exception while compiling " + e.getRobot().getPath().getAbsolutePath());
        } else {// Parse error in different (e.g. included) robot
            RobotTab tab = (RobotTab) globalController.findTab(e.getRobot().getPath());
            if (tab != null) {// RobotTab is open in editor
                tab.requestFocus();
                tab.errorPopup(e.getLine(), e.getLocalizedMessage(), e.getClass().getSimpleName(), "Exception while compiling " + e.getRobot().getPath().getAbsolutePath());
                relatedHighlightTabs.add(tab);
            } else {// RobotTab is not open in editor
                // Let's open the RobotTab where error occurred
                Platform.runLater(() -> {
                    RobotTab newTab = globalController.openRobot(e.getRobot().getPath());
                    newTab.getEditorPane().getEditor().getOnDocumentLoaded().addListener(success ->
                            // We queue this for later execution because the tab has to display before we can scroll to the right location.
                            Platform.runLater(() -> {
                                if (success) {
                                    // Highlight the tab
                                    newTab.errorPopup(e.getLine(), e.getLocalizedMessage(), e.getClass().getSimpleName(), "Exception while compiling " + e.getRobot().getPath().getAbsolutePath());
                                    relatedHighlightTabs.add(newTab);
                                }
                            })
                    );
                });
            }
        }
    }

    private void errorPopup(final int line, final String message, final String title, final String context) {
        // Create and show an error dialog.
        AlertDialog error = new AlertDialog(AlertType.ERROR, title, context, message);
        error.show();

        // Highlight the line.
        getEditorPane().getEditor().highlightLine(line, "error");
    }

    /**
     * <b>NOTE: </b> Do not save this processor over a long period as it will be swapped out often.
     *
     * @return the processor for this tab
     */
    public XillProcessor getProcessor() {
        return processor;
    }

    /**
     * Show a different currentRobot in this tab and highlight the line
     *
     * @param robot The ID of the robot
     * @param line  The line number to highlight
     */
    @SuppressWarnings("squid:S1166")
    public void display(final RobotID robot, final int line) {

        if (robot != getProcessor().getRobotID() && currentRobot == getProcessor().getRobotID()) {
            // We are moving away from the main robot
            getEditorPane().getEditor().snapshotUndoManager();
        }

        // Update the code
        if (currentRobot != robot) {

            currentRobot = robot;
            Platform.runLater(() -> {
                String code;
                try {
                    code = FileUtils.readFileToString(robot.getPath());
                } catch (IOException e) {
                    return;
                }
                // Load the code
                editorPane.getEditor().setCode(code);
                editorPane.getEditor().refreshBreakpoints(robot);

                if (robot == getProcessor().getRobotID()) {
                    // We are moving back to the current robot
                    editorPane.getEditor().restoreUndoManager();
                }

                // Blocker
                editorPane.getEditor().setEditable(currentRobot == getProcessor().getRobotID());
            });

            // Remove the 'edited' state
            Platform.runLater(() -> editorPane.getDocumentState().setValue(DocumentState.SAVED));
        }

        if (line > 0) {
            // Highlight the line
            Platform.runLater(() -> {
                editorPane.getEditor().clearHighlight();
                editorPane.getEditor().highlightLine(line, "highlight");
            });
        }
    }

    /**
     * Display the code from this tab's main currentRobot
     */
    public void resetCode() {
        display(getProcessor().getRobotID(), -1);
    }

    /**
     * @return The robot the is currently being displayed
     */
    public RobotID getCurrentRobot() {
        return currentRobot;
    }

    /**
     * @return the Statusbar
     */
    public StatusBar getStatusBar() {
        return this.apnStatusBar;
    }

    /**
     * Clear the console content related to this robot
     */
    public void clearConsolePane() {
        this.consolePane.clear();
    }

    /**
     * Clear all highlights in the current tab and in the tabs that have been highlighted from this tab (e.g. include robot error highlight)
     */
    public void clearHighlight() {
        getEditorPane().getEditor().clearHighlight();

        // Clear all highlights in all related robot tabs.
        relatedHighlightTabs.stream().filter(tab -> globalController.findTab(tab.getDocument()) != null).forEach(RobotTab::clearHighlight);
        relatedHighlightTabs.clear();
    }

    /**
     * @return the {@link RobotEditorPane} in this tab
     */
    @Override
    public RobotEditorPane getEditorPane() {
        return (RobotEditorPane) editorPane;
    }
}
