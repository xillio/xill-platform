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
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import nl.xillio.migrationtool.gui.ProjectPane;

import java.io.File;

/**
 * A dialog to add a new project from an existing source.
 */
public class LoadProjectDialog extends FXMLDialog {
    @FXML
    private TextField tfprojectname;
    @FXML
    private TextField tfprojectfolder;

    private final ProjectPane projectPane;

    /**
     * Default constructor.
     *
     * @param projectPane the projectPane to which this dialog is attached to.
     */
    public LoadProjectDialog(final ProjectPane projectPane) {
        super("/fxml/dialogs/NewProject.fxml");

        this.projectPane = projectPane;
        setTitle("New Project from source");
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

        if (projectPane.newProject(projectName, projectFolder, "", false)) {
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
