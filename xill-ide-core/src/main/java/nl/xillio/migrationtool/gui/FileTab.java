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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.SaveBeforeClosingDialog;
import nl.xillio.xill.util.settings.Settings;
import nl.xillio.xill.util.settings.SettingsHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileTab extends Tab implements Initializable, ChangeListener<EditorPane.DocumentState> {
    private static final Logger LOGGER = Log.get();
    protected static final SettingsHandler settings = SettingsHandler.getSettingsHandler();

    private Timeline autoSaveTimeline;

    @FXML
    protected EditorPane editorPane;

    protected final FXController globalController;
    protected File projectPath;
    protected File documentPath;

    /**
     * Create a new FileTab that holds a file
     *
     * @param projectPath      The project path
     * @param documentPath     The full path to the file (absolute)
     * @param globalController The FXController
     */
    public FileTab(final File projectPath, final File documentPath, final FXController globalController) {
        this("/fxml/FileTabContent.fxml", projectPath, documentPath, globalController);
    }

    /**
     * Create a new FileTab that holds a file
     *
     * @param fxml             The fxml file for this tab
     * @param projectPath      The project path
     * @param documentPath     The full path to the file (absolute)
     * @param globalController The FXController
     */
    protected FileTab(final String fxml, final File projectPath, final File documentPath, final FXController globalController) {
        this.globalController = globalController;
        this.projectPath = projectPath;
        this.documentPath = documentPath;

        if (!documentPath.isAbsolute()) {
            throw new IllegalArgumentException("The provided document must be an absolute path.");
        }

        // Load the FXML.
        try {
            setContent(Loader.load(getClass().getResource(fxml), this));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        // Add close request event handler
        setOnCloseRequest(this::onClose);
        setText(getName());

        // Add the context menu.
        addContextMenu(globalController);

        autoSaveTimeline = new Timeline(new KeyFrame(Duration.millis(1000), ae -> save(false)));

        this.setTooltip(new Tooltip(documentPath.toString()));
    }

    /**
     * Reset the autosave timeline
     */
    public void resetAutoSave() {
        if (Boolean.valueOf(settings.simple().get(Settings.SETTINGS_GENERAL, Settings.EnableAutoSave))) {
            this.autoSaveTimeline.playFromStart();
        }
    }

    private void addContextMenu(FXController controller) {
        // Close this tab.
        MenuItem closeThis = new MenuItem("Close");
        closeThis.setOnAction(e -> controller.closeTab(this));

        // Close all other tabs.
        MenuItem closeOther = new MenuItem("Close all other tabs");
        closeOther.setOnAction(e -> controller.closeAllTabsExcept(this));

        // Save
        MenuItem saveTab = new MenuItem("Save");
        saveTab.setOnAction(e -> save());

        // Save
        MenuItem saveAs = new MenuItem("Save as...");
        saveAs.setOnAction(e -> save(true));

        // Create the context menu.
        ContextMenu menu = new ContextMenu(closeThis, closeOther, saveTab, saveAs);
        this.setContextMenu(menu);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resources) {
        Platform.runLater(() -> editorPane.initialize(this));
        setText(getName());

        // Load code
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
    }

    /**
     * Transfers the focus to the editor pane.
     */
    public void requestFocus() {
        globalController.showTab(this);
        editorPane.requestFocus();
    }

    /**
     * Save this document.
     *
     * @return whether the document was saved successfully.
     */
    public boolean save() {
        return save(editorPane.getDocumentState().getValue() == EditorPane.DocumentState.NEW);
    }

    /**
     * Save this document.
     *
     * @param showDialog whether a "Save as..." dialog should be shown
     * @return whether the document was saved successfully.
     */
    protected boolean save(final boolean showDialog) {
        File document = documentPath;
        File project = projectPath;

        // Check if the project path exists.
        if (!project.exists()) {
            project = document.getParentFile();
        }

        if (showDialog) {
            // Show file picker.
            FileChooser chooser = new FileChooser();
            chooser.setInitialDirectory(project);
            String extension = FilenameUtils.getExtension(documentPath.toString());
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(extension + " file (*." + extension + ")", "*." + extension),
                    new FileChooser.ExtensionFilter("Any file (*.*)", "*.*")
            );

            // Get the selected file.
            File selected = chooser.showSaveDialog(getContent().getScene().getWindow());
            if (selected == null) {
                return false;
            }
            document = selected;
        }

        // Actually save.
        try {
            String code = editorPane.getEditor().getCodeProperty().get();
            editorPane.setLastSavedCode(code);
            FileUtils.write(document, code);
            LOGGER.info("Saved file to " + document.getAbsolutePath());
        } catch (IOException e) {
            new AlertDialog(Alert.AlertType.ERROR, "Failed to save file", "", e.getMessage()).show();
            LOGGER.error("Failed to save file", e);
        }

        // Update.
        documentPath = document.getAbsoluteFile();
        setText(getName());

        return true;
    }

    /**
     * @return the document
     */
    public File getDocument() {
        return documentPath;
    }

    /**
     * @return the name of the tab
     */
    public String getName() {
        return documentPath.getName();
    }

    /**
     * @return the globalController
     */
    public FXController getGlobalController() {
        return globalController;
    }

    protected void onClose(final Event event) {
        // Check if the robot is saved, else show the save before closing dialog.
        if (editorPane.getDocumentState().getValue() == EditorPane.DocumentState.CHANGED) {
            SaveBeforeClosingDialog dlg = new SaveBeforeClosingDialog(this, event);
            dlg.showAndWait();

            // Check if cancel was pressed.
            if (dlg.isCancelPressed()) {
                globalController.setCancelClose(true);
                return;
            }
        }

        // Check if this was the last tab.
        checkLastTab();
    }

    /**
     * Disable the save buttons if this was the last tab
     */
    protected void checkLastTab() {
        if (globalController.getTabs().size() == 1) {
            globalController.disableSaveButtons(true);
        }
    }

    @Override
    public void changed(final ObservableValue<? extends EditorPane.DocumentState> source, final EditorPane.DocumentState oldValue, final EditorPane.DocumentState newValue) {
        // This needs to happen in a JavaFX Thread.
        Platform.runLater(() -> {
            String name = getName();
            if (newValue == EditorPane.DocumentState.CHANGED) {
                name += "*";
            }
            setText(name);
        });
    }

    /**
     * @return the {@link EditorPane} in this tab
     */
    public EditorPane getEditorPane() {
        return editorPane;
    }

    /**
     * Replace the existing code in editor by the content that is in the file (it could be changed outside of editor)
     */
    public void reload() {
        if (documentPath.exists()) {
            try {
                String code = FileUtils.readFileToString(documentPath);
                editorPane.setLastSavedCode(code);
                editorPane.getEditor().setCode(code);
            } catch (IOException e) {
                LOGGER.error("Could not open " + documentPath, e);
            }
        }
    }
}
