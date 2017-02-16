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
package nl.xillio.migrationtool.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.util.settings.Settings;

import java.io.File;
import java.io.ObjectInput;

/**
 * A dialog to add a new project.
 */
public class LoadOrCreateProjectDialog extends FXMLDialog {
    @FXML
    private TextField tfprojectname;
    @FXML
    private TextField tfprojectfolder;
    @FXML
    private Label lprojectfolder;

    private final ProjectPane projectPane;
    private final String initalFoldervalue;
    private boolean hasBeenTypedInProjectFolder;

    /**
     * Default constructor.
     *
     * @param projectPane the projectPane to which this dialog is attached to.
     */
    public LoadOrCreateProjectDialog(final ProjectPane projectPane) {
        super("/fxml/dialogs/LoadOrCreateProject.fxml");

        this.projectPane = projectPane;
        setTitle("Load/Create Project");

        setProjectFolder(FXController.settings.simple().get(Settings.SETTINGS_GENERAL, Settings.DEFAULT_PROJECT_LOCATION));
        initalFoldervalue = tfprojectfolder.getText();
        tfprojectname.textProperty().addListener(this::typedInProjectName);
        tfprojectfolder.setOnKeyTyped(e -> hasBeenTypedInProjectFolder = true);
    }

    private void typedInProjectName(Object source, String oldValue, String newValue) {
        // The name changed. See if we need to fix this in the project folder field.
        // This we only do if the project folder field remains untouched
        if (isProjectFolderUntouched(tfprojectfolder.getText())) {
            setProjectFolder(initalFoldervalue + File.separator + newValue);
        }
    }

    private boolean isProjectFolderUntouched(String newValue) {
        // If the project value equals the initial value then it is untouched
        if (newValue.equals(initalFoldervalue)) {
            hasBeenTypedInProjectFolder = false;
            return true;
        }

        // If anyone typed in this field it has been touched
        if (hasBeenTypedInProjectFolder) {
            return false;
        }

        // If the initial value isn't the prefix anymore it has been touched
        if (!newValue.startsWith(initalFoldervalue)) {
            return false;
        }

        int lastIndex = newValue.lastIndexOf(File.separatorChar);
        return newValue.substring(0, lastIndex).equals(initalFoldervalue);
    }

    /**
     * Set the default project folder
     *
     * @param folder project folder
     */
    public void setProjectFolder(final String folder) {
        tfprojectfolder.setText(folder);
    }

    @FXML
    private void browseBtnPressed(final ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(getInitialDirectory());


        File result = chooser.showDialog(getScene().getWindow());
        if (result != null) {
            tfprojectfolder.setText(result.getPath());

            // If we have no project name yet we want to auto fill this
            if (tfprojectname.getText().isEmpty()) {
                tfprojectname.setText(result.getName());
            }
        }
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        String projectName = tfprojectname.getText();
        String projectFolder = tfprojectfolder.getText();

        if (projectPane.loadOrCreateProject(projectName, projectFolder)) {
            close();
        }
    }

    public File getInitialDirectory() {
        // Set directory
        if (tfprojectfolder.getText().isEmpty()) {
            return new File(System.getProperty("user.home"));
        }

        File currentFolder = new File(tfprojectfolder.getText());
        while (currentFolder != null && !currentFolder.exists()) {
            currentFolder = currentFolder.getParentFile();
        }

        return currentFolder;
    }
}