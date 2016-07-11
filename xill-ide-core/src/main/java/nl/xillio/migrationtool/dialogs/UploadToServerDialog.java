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

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Pair;
import me.biesaart.utils.FileUtils;
import nl.xillio.migrationtool.XillServerUploader;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.util.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A dialog to upload an item to the server.
 */
public class UploadToServerDialog extends FXMLDialog {

    @FXML
    private TextField tfserver;
    @FXML
    private TextField tfusername;
    @FXML
    private TextField tfpassword;

    private final ObservableList<TreeItem<Pair<File, String>>> treeItems;
    private final ProjectPane projectPane;
    private final XillServerUploader xillServerUploader = new XillServerUploader();

    /**
     * Default constructor.
     *
     * @param projectPane the projectPane to which this dialog is attached to.
     * @param treeItems   the tree item on which the item will be deleted
     */
    public UploadToServerDialog(final ProjectPane projectPane, final ObservableList<TreeItem<Pair<File, String>>> treeItems) {
        super("/fxml/dialogs/UploadToServer.fxml");
        this.treeItems = treeItems;
        this.projectPane = projectPane;

        setTitle("Upload to server");

        tfserver.setText(FXController.settings.simple().get(Settings.SERVER, Settings.XillServerHost));
        tfusername.setText(FXController.settings.simple().get(Settings.SERVER, Settings.XillServerUsername));
        tfpassword.setText(FXController.settings.simple().get(Settings.SERVER, Settings.XillServerPassword));
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        try {
            // Do connect and authenticate to server
            xillServerUploader.authenticate(tfserver.getText(), tfusername.getText(), tfpassword.getText());
        } catch (IOException e) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Upload to server",
                    "Could not login to server.", e.getMessage(),
                    ButtonType.OK);
            dialog.showAndWait();
            return;
        }

        try {
            // Save current credentials
            FXController.settings.simple().save(Settings.SERVER, Settings.XillServerHost, tfserver.getText());
            FXController.settings.simple().save(Settings.SERVER, Settings.XillServerUsername, tfusername.getText());
            FXController.settings.simple().save(Settings.SERVER, Settings.XillServerPassword, tfpassword.getText());

            // Process items
            if (!processItems(treeItems, true, true, null)) {
                return; // Process has been user interrupted - so no success dialog is shown
            }
        } catch (Exception e) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Upload to server",
                    "Uploading process has failed.", e.getMessage() + "\n" + e.getCause().getMessage(),
                    ButtonType.OK);
            dialog.showAndWait();
            return;
        }

        AlertDialog dialog = new AlertDialog(Alert.AlertType.INFORMATION, "Upload to server",
                "Uploading process has been successfully finished.", null,
                ButtonType.OK);
        dialog.showAndWait();

        close();
    }

    private boolean processItems(final List<TreeItem<Pair<File, String>>> items, boolean projectExistCheck, boolean robotExistCheck, final String projectId) throws IOException, JsonException {
        // Recursively go through selected items
        for (TreeItem<Pair<File, String>> item : items) {
            // Check if the item is a project
            if (item.getParent() == projectPane.getRoot()) {// Project
                if (!uploadProject(item, projectExistCheck)) {
                    return false;
                }
            } else if (item.getValue().getKey().isDirectory()) {// Directory
                // Upload robots inside the directory
                if (!uploadProject(item, false)) {
                    return false;
                }
            } else {// Robot or resource
                if (!uploadItem(item, robotExistCheck, projectId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean uploadProject(final TreeItem<Pair<File, String>> item, boolean existCheck) throws IOException, JsonException {
        final File projectFolder = projectPane.getProject(item).getValue().getKey();
        final String projectName = xillServerUploader.getProjectName(projectFolder);

        String projectId = xillServerUploader.findProject(projectName);

        // Check for project existence on the server
        if (existCheck && projectId != null) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "Uploading project",
                    String.format("The project %1$s already exists on the server", projectName), "Do you want to overwrite entire project?",
                    ButtonType.YES, ButtonType.NO);
            if (dialog.showAndWait().get().getButtonData() == ButtonBar.ButtonData.NO) {
                return false;
            }
            // Yes, the existing project will be overwritten
            xillServerUploader.deleteProject(projectId); // Delete the project on the server
        }
        projectId = xillServerUploader.ensureProjectExist(projectName);

        // Upload all robots
        return processItems(item.getChildren(), false, false, projectId);
    }

    private boolean uploadItem(final TreeItem<Pair<File, String>> item, boolean existCheck, final String projectId) throws IOException, JsonException {
        // Get the selected item info
        final File itemFile = item.getValue().getKey();
        final File projectFolder = projectPane.getProject(item).getValue().getKey();

        // Determine if the item is robot or resource
        if (isRobot(itemFile, projectFolder)) {
            return uploadRobot(itemFile, projectFolder, existCheck, projectId);
        } else {
            return uploadResource(itemFile, projectFolder, existCheck, projectId);
        }
    }

    /**
     * Determine if item is robot or resource.
     *
     * A robot has a fully qualified name if:
     *  - All folder names above it (up to and excluding the project folder) are valid package names. Regex: ([a-zA-Z][a-zA-Z0-9_]*)
     *  - The robot itself is a valid robot name ([a-zA-Z][a-zA-Z0-9_]*) and ends with the .xill extension
     * All other files (including Xill scripts that do not match the rules above) are considered resources.
     *
     * @param itemFile the file to be determined
     * @return true if it's robot, otherwise it's a resource
     */
    private boolean isRobot(final File itemFile, final File projectFolder) {
        // Get inner path
        final String innerPath = projectFolder.toURI().relativize(itemFile.getParentFile().toURI()).getPath();

        // Validate inner path if it's compliant for being part of robot's FQN
        if (!innerPath.isEmpty()) {
            for (String part : innerPath.split("/")) {
                if (!part.matches("[a-zA-Z][a-zA-Z0-9_]*")) { // Check each folder name in inner path if it is a valid package name
                    return false; // No, it's a resource
                }
            }
        }

        // Validate robot name
        return itemFile.getName().matches("^[a-zA-Z][a-zA-Z0-9_]*\\.xill$");
    }

    private boolean uploadRobot(final File robotFile, final File projectFolder, final boolean existCheck, String projectId) throws IOException, JsonException {
        final String code;
        code = FileUtils.readFileToString(robotFile);

        final String robotFqn = xillServerUploader.getFqn(robotFile, projectFolder);
        if (projectId == null) {
            projectId = xillServerUploader.ensureProjectExist(xillServerUploader.getProjectName(projectFolder));
        }

        // Check for robot existence on the server
        if (existCheck && xillServerUploader.existRobot(projectId, robotFqn)) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "Uploading robot",
                    String.format("The robot %1$s already exists on the server", robotFile.getName()), "Do you want to overwrite it?",
                    ButtonType.YES, ButtonType.NO);
            if (dialog.showAndWait().get().getButtonData() == ButtonBar.ButtonData.NO) {
                return false;
            }
            // Yes, the existing robot will be overwritten
        }

        // Upload the robot
        xillServerUploader.uploadRobot(projectId, robotFqn, code);
        return true;
    }

    private boolean uploadResource(final File resourceFile, final File projectFolder, final boolean existCheck, String projectId) throws JsonException, IOException {
        final String resourceName = xillServerUploader.getResourceName(resourceFile, projectFolder);
        if (projectId == null) {
            projectId = xillServerUploader.ensureProjectExist(xillServerUploader.getProjectName(projectFolder));
        }

        // Check for resource existence on the server
        if (existCheck && xillServerUploader.existResource(projectId, resourceName)) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "Uploading resource",
                    String.format("The resource %1$s already exists on the server", resourceName), "Do you want to overwrite it?",
                    ButtonType.YES, ButtonType.NO);
            if (dialog.showAndWait().get().getButtonData() == ButtonBar.ButtonData.NO) {
                return false;
            }
            // Yes, the existing resource will be overwritten
        }

        // Upload the resource
        xillServerUploader.uploadResource(projectId, resourceName, resourceFile);
        return true;
    }
}
