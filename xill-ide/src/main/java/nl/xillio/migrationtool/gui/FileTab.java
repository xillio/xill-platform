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
import me.biesaart.utils.IOUtils;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.Loader;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.SaveBeforeClosingDialog;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.util.settings.Settings;
import nl.xillio.xill.util.settings.SettingsHandler;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.ResourceBundle;

public class FileTab extends Tab implements Initializable, ChangeListener<EditorPane.DocumentState> {
    protected static final SettingsHandler settings = SettingsHandler.getSettingsHandler();
    private static final Logger LOGGER = Log.get();
    private final FXController globalController;
    @FXML
    private EditorPane editorPane;
    private final Timeline autoSaveTimeline;
    private URL resourceUrl;

    /**
     * Create a new FileTab that holds a file.
     *
     * @param resourceUrl      The full path to the file (absolute)
     * @param globalController The FXController
     */
    public FileTab(final URL resourceUrl, final FXController globalController) {
        this("/fxml/FileTabContent.fxml", resourceUrl, globalController);
    }

    /**
     * Create a new FileTab that holds a file.
     *
     * @param fxml             The fxml file for this tab
     * @param resourceUrl      The full path to the file (absolute)
     * @param globalController The FXController
     */
    protected FileTab(final String fxml, final URL resourceUrl, final FXController globalController) {
        this.resourceUrl = resourceUrl;
        this.globalController = globalController;
        Objects.nonNull(resourceUrl);

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

        this.setTooltip(new Tooltip(resourceUrl.toString()));
    }

    /**
     * Reset the autosave timeline
     */
    public void resetAutoSave() {
        if (Boolean.valueOf(settings.simple().get(Settings.SETTINGS_GENERAL, Settings.ENABLE_AUTO_SAVE))) {
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
        reload();

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
        URL saveDocument = resourceUrl;

        if (showDialog) {
            saveDocument = pickFile();
        }

        if (saveDocument == null) {
            // We have no target to save to, so the save failed.
            return false;
        }

        String unescapeResource;
        try {
            unescapeResource = URLDecoder.decode(saveDocument.getFile().replace("+", "%2B"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unhandled character set found.", e);
        }

        return saveCodeTo(unescapeResource, saveDocument.getProtocol());
    }

    private boolean saveCodeTo(String url, String protocol) {
        try {
            if ("file".equals(protocol)) {
                try (OutputStream output = new FileOutputStream(new File(url))) {
                    String code = editorPane.getEditor().getCodeProperty().get();
                    IOUtils.write(code, output);
                    editorPane.setLastSavedCode(code);
                }
            } else {
                throw new IllegalArgumentException("Failed to save file, invalid protocol: " + protocol);
            }

            return true;
        } catch (IOException e) {
            AlertDialog error = new AlertDialog(Alert.AlertType.ERROR, "Failed to save file", "", e.getMessage() + ".");
            error.show();
            LOGGER.error("Failed to save file", e);
            return false;
        }
    }

    private URL pickFile() {
        try {
            File editingFile = new File(resourceUrl.toURI());
            File parentFolder = editingFile.getParentFile();

            FileChooser chooser = new FileChooser();
            chooser.setInitialDirectory(parentFolder);
            String extension = FilenameUtils.getExtension(editingFile.getName());
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter(extension + " file (*." + extension + ")", "*." + extension),
                    new FileChooser.ExtensionFilter("All files (*.*)", "*.*")
            );

            // Get the selected file.
            File selected = chooser.showSaveDialog(getContent().getScene().getWindow());
            if (selected == null) {
                return null;
            }

            // If the selected file has no extension, add the robot extension.
            if (FilenameUtils.getExtension(selected.toString()).isEmpty()) {
                selected = new File(selected.toString() + XillEnvironment.ROBOT_EXTENSION);
            }

            return selected.toURI().toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    /**
     * @return the name of the tab
     */
    public String getName() {
        try {
            String unescapedPath = URLDecoder.decode(resourceUrl.getFile().replace("+", "%2B"), "UTF-8");
            return FilenameUtils.getName(unescapedPath);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unhandled character set found.", e);
        }
    }

    /**
     * @return the globalController
     */
    public FXController getGlobalController() {
        return globalController;
    }

    public URL getResourceUrl() {
        return resourceUrl;
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
        reload(resourceUrl, true);
    }

    /**
     * Replace the existing code in editor by the content that is in the file (it could be changed outside of editor)
     */
    public void reload(URL resource, boolean resetLastSave) {
        try (InputStream stream = resource.openStream()) {
            String code = IOUtils.toString(stream);
            if (resetLastSave) {
                editorPane.setLastSavedCode(code);
            }
            editorPane.getEditor().setCode(code);
        } catch (IOException e) {
            LOGGER.info("Could not open " + resourceUrl, e);
            Platform.runLater(() -> getGlobalController().closeTab(this));
        }
    }

    public void resetSource(URL resourceUrl) {
        this.resourceUrl = resourceUrl;
        reload();
    }
}
