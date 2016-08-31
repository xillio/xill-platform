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
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.BreakpointPool;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.gui.editor.AceEditor;
import nl.xillio.xill.util.HotkeysHandler;
import nl.xillio.xill.util.settings.Settings;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The editor pane for files. Contains most of the UI, apart from the left panel.
 */
public class EditorPane extends AnchorPane implements FileTabComponent, EventHandler<KeyEvent>, ChangeListener<String> {
    private static final Logger LOGGER = Log.get();

    protected final AceEditor editor;
    protected final Property<DocumentState> documentState = new SimpleObjectProperty<>(DocumentState.NEW);

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
    private Button btnRemoveAllBreakpoints;
    @FXML
    protected ReplaceBar editorReplaceBar;

    protected FileTab tab;

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
        this("/fxml/EditorPane.fxml");
    }

    protected EditorPane(String fxml) {
        try {
            getChildren().add(Loader.load(getClass().getResource(fxml), this));
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
        editor.getOnDocumentLoaded().addListener(e -> editor.autoDetectMode(tab.getDocument().getName()));

        btnRedo.setDisable(true);
        btnUndo.setDisable(true);

        addEventHandler(KeyEvent.KEY_PRESSED, this);
    }

    @Override
    public void initialize(final FileTab tab) {
        this.tab = tab;

        editor.setTab(tab);
        editorReplaceBar.getOnClose().addListener(clear -> {
            if (clear) {
                requestFocus();
            }
        });

        editor.loadEditor();
        editor.setOptions(tab.getGlobalController().createEditorOptionsJSCode());
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

    public void print() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob == null) {
            LOGGER.error("Could not create printer job.");
            return;
        }

        // This is commented out as it's not working properly (will be resolved in Java 9) see: https://bugs.openjdk.java.net/browse/JDK-8088395
//        if (!printerJob.showPrintDialog(this.getParent().getScene().getWindow())) {
//            return;
//        }

        print(getEditor().getCodeProperty().get(), printerJob);
    }

    private void print(final String content, final PrinterJob printerJob) {
        // Get all lines
        final String[] lines = content.split(System.getProperty("line.separator"));

        // Get printer and page settings
        JobSettings jobSettings = printerJob.getJobSettings();
        PageLayout pageLayout = jobSettings.getPageLayout();

        // Prepare JavaFX Text node
        final Text text = new Text();
        text.setFont(new Font(Integer.valueOf(FXController.settings.simple().get(Settings.SETTINGS_EDITOR, Settings.PRINTER_FONT_SIZE))));
        text.setTextAlignment(TextAlignment.LEFT);
        text.setWrappingWidth(pageLayout.getPrintableWidth());

        // Do print
        String pageText = "";
        final double maxHeight = pageLayout.getPrintableHeight();
        double curHeight = 0;
        for (String line : lines) {// Walk through each line of text
            text.setText(line);
            double lineHeight = text.getLayoutBounds().getHeight(); // Calculate line height (it can be multiline because of wrapping)
            if (lineHeight + curHeight > maxHeight) {
                // This line is beyond the page height, so do the print page
                text.setText(pageText);
                printerJob.printPage(text);

                // Clear the internal page
                curHeight = 0;
                pageText = "";
            }

            // Add line to internal page
            pageText += "\n";
            pageText += line;
            curHeight += lineHeight;
        }
        if (!pageText.isEmpty()) {
            // Print the rest as a last page (if there is any to print)
            text.setText(pageText);
            printerJob.printPage(text);
        }

        printerJob.endJob();
    }

    @FXML
    private void buttonRedo() {
        if (!btnRedo.isDisabled()) {
            editor.redo();
        }
        updateUndoRedoButtons();
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
            // Open the search bar.
            if (!editorReplaceBar.isOpen()) {
                editorReplaceBar.open(1);
            }

            // If there is selected text, put it in the search bar.
            String selected = editor.getSelectedText();
            if (!selected.isEmpty()) {
                editorReplaceBar.setSearchText(selected);
            }

            editorReplaceBar.requestFocus();
            event.consume();
        } else if (KeyCombination.valueOf(FXController.hotkeys.getShortcut(HotkeysHandler.Hotkeys.PRINT)).match(event)) {
            // Print.
            print();
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
     * @param newCode the new code
     */
    protected void updateDocumentState(final String newCode) {
        if (lastSavedCode.equals(newCode)) {
            documentState.setValue(DocumentState.SAVED);
        } else {
            documentState.setValue(DocumentState.CHANGED);
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
