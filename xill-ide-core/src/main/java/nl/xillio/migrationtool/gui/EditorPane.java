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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.BreakpointPool;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.elasticconsole.ESConsoleClient;
import nl.xillio.migrationtool.elasticconsole.RobotLogMessage;
import nl.xillio.migrationtool.gui.StatusBar.Status;
import nl.xillio.migrationtool.gui.editor.AceEditor;
import nl.xillio.xill.util.HotkeysHandler;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The editor pane. Contains most of the UI, apart from the left panel.
 */
public class EditorPane extends AnchorPane implements EventHandler<KeyEvent>, RobotTabComponent, ChangeListener<String> {

    private static final Logger LOGGER = Log.get();
    private final AceEditor editor;
    private final Property<DocumentState> documentState = new SimpleObjectProperty<>(DocumentState.NEW);
    @FXML
    private Button btnUndo;
    @FXML
    private Button btnRedo;
    @FXML
    private Button btnBrowser;
    @FXML
    private Button btnRegexTester;
    @FXML
    private Button btnPreviewOpenBrowser;
    @FXML
    private Button btnPreviewOpenRegex;
    @FXML
    private CheckMenuItem cmiDebug;
    @FXML
    private CheckMenuItem cmiInfo;
    @FXML
    private CheckMenuItem cmiWarning;
    @FXML
    private CheckMenuItem cmiError;
    @FXML
    private Button btnRemoveAllBreakpoints;
    @FXML
    private Button btnRun;
    @FXML
    private Button btnStepOver;
    @FXML
    private Button btnStepIn;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnStop;
    @FXML
    private ReplaceBar editorReplaceBar;
    private RobotControls controls;
    private RobotTab tab;

    /**
     * Contains the editor's content (Xill source code) that was saved to disc (or whatever medium) last time.
     */
    private String lastSavedCode = "";

    /**
     * Contains the code content that was detected last time as changed outside the editor (the file with robot was changed outside of editor - this contains last content of that file)
     * It's needed to store this because after one real file content change, the system can fire multiple events for that and this is a way how not to display multiple query dialogs for one file change..
     */
    private String lastChangedCode = "";

    /**
     * Default constructor. Just sets up the UI and the listener.
     */
    public EditorPane() {
        try {
            getChildren().add(Loader.load(getClass().getResource("/fxml/EditorPane.fxml"), this));
        } catch (IOException e) {
            LOGGER.error("Error loading editor pane: " + e.getMessage(), e);
        }

        editor = new AceEditor((WebView) lookup("#webCode"));

        ToolBar tlbSearchToolBar = (ToolBar) lookup("#tlbSearchToolBar");
        ToggleButton tbnEditorSearch = (ToggleButton) tlbSearchToolBar.getItems().stream().filter(child -> "tbnEditorSearch".equals(child.getId())).findAny().get();

        editorReplaceBar.setSearchable(editor);
        editorReplaceBar.setButton(tbnEditorSearch, 1);
        editor.getCodeProperty().addListener(this);
        editor.setReplaceBar(editorReplaceBar);

        btnRedo.setDisable(true);
        btnUndo.setDisable(true);

        addEventHandler(KeyEvent.KEY_PRESSED, this);
    }

    @Override
    public void initialize(final RobotTab tab) {

        this.tab = tab;
        controls = new RobotControls(tab, btnRun, btnPause, btnStop, btnStepIn, btnStepOver, cmiError);
        editor.setTab(tab);
        editorReplaceBar.getOnClose().addListener(clear -> {
            if (clear) {
                requestFocus();
            }
        });

        editor.loadEditor();
        editor.setOptions(tab.getGlobalController().createEditorOptionsJSCode());
        ESConsoleClient.getLogEvent(tab.getProcessor().getRobotID()).addListener(this::onLogMessage);
    }

    /**
     * Update the state of the undo/redo buttons.
     * 
     * Due to some strange behaviour when using the delete key on a selection after opening a robot NOT updating the
     * editor state immediately, the task is run a couple of times to ensure the state is updated correctly.
     */
    private void updateUndoRedoButtons() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            private int repeated = 0;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    btnRedo.setDisable(!editor.hasRedo());
                    btnUndo.setDisable(!editor.hasUndo());
                });
                if (repeated++ > 3) {
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 500);

    }

    @FXML
    private void buttonUndo() {
        if (!btnUndo.isDisabled()) {
            editor.undo();
        }
        updateUndoRedoButtons();
    }

    @FXML
    private void buttonRedo() {
        if (!btnRedo.isDisabled()) {
            editor.redo();
        }
        updateUndoRedoButtons();
    }

    private void onLogMessage(final RobotLogMessage message) {
        switch (message.getLevel()) {
            case "debug":
                if (cmiDebug.isSelected()) {
                    controls.pause(false);
                }
                break;
            case "info":
                if (cmiInfo.isSelected()) {
                    controls.pause(false);
                }
                break;
            case "warn":
                if (cmiWarning.isSelected()) {
                    controls.pause(false);
                }
                break;
            case "error":
                if (cmiError.isSelected()) {
                    controls.pause(false);
                }
                break;
            default:
                LOGGER.debug("Unimplemented loglevel: " + message.getLevel());
                break;
        }
    }

    /**
     * Overrides requestFocus by transferring the focus to the webCode.
     *
     * @see javafx.scene.Node#requestFocus()
     */
    @Override
    public void requestFocus() {
        editor.requestFocus();
    }

    @Override
    @FXML
    public void handle(final KeyEvent event) {
        // Find
        if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(HotkeysHandler.Hotkeys.FIND)).match(event)) {
            event.consume();
            if (!editorReplaceBar.isOpen()) {
                editorReplaceBar.open(1);
            }

            String selected = editor.getSelectedText();
            if (!selected.isEmpty()) {
                editorReplaceBar.setSearchText(selected);
            }
            editorReplaceBar.requestFocus();
        } else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(HotkeysHandler.Hotkeys.HELP)).match(event)) {
            String[] values;
            try {
                values = getEditor().getPluginAndConstructAtCursor();
            } catch (ClassCastException e) {
                LOGGER.warn(e.getMessage());
                return;
            }

            HelpPane helpPane = tab.getGlobalController().getHelpPane();
            if (values[0] == null) {
                helpPane.getHelpSearchBar().cleanup();
                helpPane.displayHome();
                helpPane.getHelpSearchBar().requestFocus();
            } else if (values[1] == null) {
                helpPane.display(values[0], "_index");
            } else {
                helpPane.display(values[0], values[1]);
            }
            event.consume();
        }
    }

    /**
     * Perform Ace editor settings
     *
     * @param jsCode JavaScript code with settings to be executed
     */
    public void setEditorOptions(final String jsCode) {
        Platform.runLater(() -> editor.setOptions(jsCode));
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    public AceEditor getEditor() {
        return editor;
    }

    /**
     * Returns the state of the document.
     *
     * @return the state of the document
     */
    public Property<DocumentState> getDocumentState() {
        return documentState;
    }

    @FXML
    private void buttonRemoveAllBreakpoints() {
        BreakpointPool.INSTANCE.clear();
        tab.getGlobalController().getTabs().forEach(editorTab -> editorTab.getEditorPane().getEditor().clearBreakpoints());
    }

    @Override
    public void changed(final ObservableValue<? extends String> source, final String oldValue, final String newValue) {
        updateUndoRedoButtons();
        updateDocumentState(newValue);
    }

    /**
     * Checks if the @newCode means that document is changed or not.
     * It compares the @newCode with the last saved editor's content.
     *
     * @param newCode the Xill source code
     */
    private void updateDocumentState(final String newCode) {
        if (lastSavedCode.equals(newCode)) {
            documentState.setValue(DocumentState.SAVED);
        } else {
            documentState.setValue(DocumentState.CHANGED);

            // Only trigger auto-save if we are ready to run
            Status currentStatus = tab.getStatusBar().statusProperty().get();

            if (currentStatus == null || currentStatus == Status.PAUSED || currentStatus == Status.READY || currentStatus == Status.STOPPED) {
                tab.resetAutoSave();
            }

        }
    }

    /**
     * It saves the last saved editor's content - for the later usage when determining the state of the document
     *
     * @param newCode the last saved editor's content
     */
    public void setLastSavedCode(final String newCode) {
        lastSavedCode = newCode;
        documentState.setValue(DocumentState.SAVED);
    }

    /**
     * Checks if the provided code is an update on what the editor thinks is on disk
     *
     * @param newChangedCode the source code that is currently saved in the robot file
     * @return true if the provided code does not match what we have
     */
    public boolean checkChangedCode(final String newChangedCode) {

        if (lastSavedCode.equals(newChangedCode)) {
            return false; // The new changed source code is the same as existing
        }

        // The new changed source code is different from existing in editor
        if (lastChangedCode.equals(newChangedCode)) {
            // But the new changed source code is the same as the last changed source code - so no change here
            return false;
        } else {
            lastChangedCode = newChangedCode;
            return true;
        }

    }

    /**
     * @return the robot controls which allows to control the active robot
     */
    public RobotControls getControls() {
        return controls;
    }

    /**
     * The different states a robot can have.
     */
    public enum DocumentState {
        /**
         * State of a robot that is created but not saved yet.
         */
        NEW, /**
         * State of a saved robot that has been modified and not saved yet.
         */
        CHANGED, /**
         * State of a robot that is saved and not modified.
         */
        SAVED
    }
}
