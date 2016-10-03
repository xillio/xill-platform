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
import me.biesaart.utils.Log;
import me.biesaart.utils.StringUtils;
import nl.xillio.migrationtool.RobotValidationException;
import nl.xillio.migrationtool.XillServerUploader;
import nl.xillio.migrationtool.gui.FXController;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.util.settings.Settings;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A dialog to upload an item to the server.
 */
public class UploadToServerDialog extends FXMLDialog {

    private static final Logger LOGGER = Log.get();

    @FXML
    private TextField tfserver;
    @FXML
    private TextField tfusername;
    @FXML
    private TextField tfpassword;
    @FXML
    private CheckBox cbvalidate;

    private final ObservableList<TreeItem<Pair<File, String>>> treeItems;
    private final ProjectPane projectPane;
    private final XillServerUploader xillServerUploader = new XillServerUploader();
    private List<String> uploadedRobots = new LinkedList<>();
    private String uploadedProjectId = "";
    private List<String> invalidRobots = new LinkedList<>();

    private static final String MAX_FILE_SIZE_SETTINGS_KEY = "spring.http.multipart.max-file-size";
    private long maxFileSize; // Maximum file size that Xill server accepts

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

        tfserver.setText(FXController.settings.simple().get(Settings.SERVER, Settings.XILL_SERVER_HOST));
        tfusername.setText(FXController.settings.simple().get(Settings.SERVER, Settings.XILL_SERVER_USERNAME));
        tfpassword.setText(FXController.settings.simple().get(Settings.SERVER, Settings.XILL_SERVER_PASSWORD));
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
            LOGGER.error("Could not login to server", e);
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Upload to server",
                    "Could not login to server.", e.getMessage(),
                    ButtonType.OK);
            dialog.showAndWait();
            return;
        }

        try {
            // Save current credentials
            FXController.settings.simple().save(Settings.SERVER, Settings.XILL_SERVER_HOST, tfserver.getText());
            FXController.settings.simple().save(Settings.SERVER, Settings.XILL_SERVER_USERNAME, tfusername.getText());
            FXController.settings.simple().save(Settings.SERVER, Settings.XILL_SERVER_PASSWORD, tfpassword.getText());

            // Clean up
            uploadedRobots.clear();
            invalidRobots.clear();

            // Get current server settings
            queryServerSettings();

            // Process items (do selected robots and resources size check only prior to upload itself starts)
            if (!processItems(treeItems, true, null, true)) {
                return; // Process has been user interrupted - so no success dialog is shown
            }
            if (!invalidRobots.isEmpty()) { // Some robots have invalid name
                InvalidRobotNamesDialog dialog = new InvalidRobotNamesDialog(StringUtils.join(invalidRobots.toArray(), '\n'));
                dialog.showAndWait();
                if (dialog.isCanceled()) {
                    return; //cancel uploading process
                }
            }

            // Process items (do selected robots and resources upload)
            if (!processItems(treeItems, true, null, false)) {
                return; // Process has been user interrupted - so no success dialog is shown
            }

            // Validate uploaded robots (in a one bulk server request - because of every Xill environment start is very time consuming)
            if (cbvalidate.isSelected() && !uploadedRobots.isEmpty()) {
                xillServerUploader.validateRobots(uploadedProjectId, uploadedRobots);
            }

            // Show success message
            AlertDialog dialog = new AlertDialog(Alert.AlertType.INFORMATION, "Upload to server",
                    "Uploading process has been successfully finished.", null,
                    ButtonType.OK);
            dialog.showAndWait();

            // Close the dialog
            close();

        } catch (IOException e) {
            LOGGER.error("Uploading process has failed", e);
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Upload to server",
                    "Uploading process has failed.", e.getMessage() + (e.getCause() == null ? "" : "\n" + e.getCause().getMessage()),
                    ButtonType.OK);
            dialog.showAndWait();
        } catch (RobotValidationException e) {
            LOGGER.error("At least one uploaded robot is not valid", e);
            AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "Upload to server",
                    "At least one uploaded robot is not valid.", e.getMessage(),
                    ButtonType.OK);
            dialog.showAndWait();
        }
    }

    /**
     * Iterate given level of tree items, process them and recursively iterate and process all child tree levels.
     *
     * @param items         The list of items in one tree level (the first call means base level and the next recursive calls mean child levels).
     * @param existCheck    If true then it will check for project or robot existence on the server and ask user about overwriting it if already exists.
     *                       This (true option) is used when the first level of tree is being processed.
     *                      If false then the check for project or robot existence on the server is not carried out.
     *                       This (false option) is used when processing the child levels of the tree.
     * @param projectId     The identificator of the project in the server database.
     *                       If projectId is null it means the id is not known yet and will be determined by querying the server using project folder name.
     * @param noUpload      If true then all requests to server will not be carried out.
     *                       This (true option) is used for iterating of all tree items (including all child items) and checking if their size does not exceed limit.
     * @return              True if processing was successful. False if processing failed and will be stopped completely.
     * @throws IOException  In case of communication error with the server or some other IO error.
     */
    private boolean processItems(final List<TreeItem<Pair<File, String>>> items, boolean existCheck, final String projectId, boolean noUpload) throws IOException {
        // Recursively go through selected items
        for (TreeItem<Pair<File, String>> item : items) {
            // Check if the item is a project
            if (item.getParent() == projectPane.getRoot()) {// Project
                if (!uploadProject(item, existCheck, noUpload)) {
                    return false;
                }
            } else if (item.getValue().getKey().isDirectory()) {// Directory
                // Upload items from inside the directory
                if (!uploadFolder(item, existCheck, noUpload)) {
                    return false;
                }
            } else {// Robot or resource
                if (!uploadItem(item, existCheck, projectId, noUpload)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void queryServerSettings() throws IOException {
        Map<String, String> settings = xillServerUploader.querySettings();
        if (!settings.containsKey(MAX_FILE_SIZE_SETTINGS_KEY)) {
            throw new IOException("Invalid response from the server.");
        }
        maxFileSize = Long.valueOf(settings.get(MAX_FILE_SIZE_SETTINGS_KEY));
    }

    private boolean isInvalidRobotFile(TreeItem<Pair<File, String>> item) {
        final File itemFile = item.getValue().getKey();
        final boolean isXill = itemFile.getName().matches("^.*\\.xill$"); //it is a robot
        final boolean validName = itemFile.getName().matches("^[_a-zA-Z][a-zA-Z0-9_]*\\.xill$"); //name is valid
        return (isXill && !validName);
    }

    /**
     * Uploads entire project to Xill server.
     *
     * @param item          The project tree item.
     * @param existCheck    True if project existence check should be done (see {@link UploadToServerDialog#processItems} for details).
     * @param noUpload      True if all request to server will be not carried out (see {@link UploadToServerDialog#processItems} for details).
     * @return              True if processing was successful. False if processing failed and will be stopped completely.
     * @throws IOException  In case of communication error with the server or some other IO error.
     */
    private boolean uploadProject(final TreeItem<Pair<File, String>> item, boolean existCheck, boolean noUpload) throws IOException {
        final File projectFolder = projectPane.getProject(item).getValue().getKey();
        final String projectName = xillServerUploader.getProjectName(projectFolder);

        String projectId = xillServerUploader.findProject(projectName);

        if (!noUpload) {
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
        }

        // Upload all project items
        return processItems(item.getChildren(), false, projectId, noUpload);
    }

    /**
     * Uploads robot or resource to Xill server.
     *
     * @param item          The robot or resource tree item.
     * @param existCheck    True if robot or resource existence check should be done (see {@link UploadToServerDialog#processItems} for details).
     * @param noUpload      True if all request to server will be not carried out (see {@link UploadToServerDialog#processItems} for details).
     * @return              True if processing was successful. False if processing failed and will be stopped completely.
     * @throws IOException  In case of communication error with the server or some other IO error.
     */
    private boolean uploadItem(final TreeItem<Pair<File, String>> item, boolean existCheck, final String projectId, boolean noUpload) throws IOException {

        // Get the selected item info
        final File itemFile = item.getValue().getKey();
        final File projectFolder = projectPane.getProject(item).getValue().getKey();

        // Do test for invalid robot name
        if (isInvalidRobotFile(item)) {
            if (noUpload) {
                invalidRobots.add(xillServerUploader.getFqn(itemFile, projectFolder));
            } else {
                return true; // Continue - i.e. skip uploading this robot (with invalid name)
            }
        }

        // Determine if the item is robot or resource
        if (isRobot(itemFile, projectFolder)) {
            return uploadRobot(itemFile, projectFolder, existCheck, projectId, noUpload);
        } else {
            return uploadResource(itemFile, projectFolder, existCheck, projectId, noUpload);
        }
    }

    /**
     * Uploads folder to Xill server.
     *
     * @param item          The folder tree item.
     * @param existCheck    True if project existence check should be done (see {@link UploadToServerDialog#processItems} for details).
     * @param noUpload      True if all request to server will be not carried out (see {@link UploadToServerDialog#processItems} for details).
     * @return              True if processing was successful. False if processing failed and will be stopped completely.
     * @throws IOException  In case of communication error with the server or some other IO error.
     */
    private boolean uploadFolder(final TreeItem<Pair<File, String>> item, boolean existCheck, boolean noUpload) throws IOException {
        final File projectFolder = projectPane.getProject(item).getValue().getKey();
        final String projectName = xillServerUploader.getProjectName(projectFolder);

        String projectId = xillServerUploader.findProject(projectName);

        if (!noUpload) {
            // Check for project existence on the server
            if (existCheck && projectId != null) {
                AlertDialog dialog = new AlertDialog(Alert.AlertType.WARNING, "Uploading folder",
                        String.format("The project %1$s already exists on the server", projectName), "Do you want to upload the folder content?",
                        ButtonType.YES, ButtonType.NO);
                if (dialog.showAndWait().get().getButtonData() == ButtonBar.ButtonData.NO) {
                    return false;
                }
            }
            projectId = xillServerUploader.ensureProjectExist(projectName);
        }

        // Upload all items from within the folder
        return processItems(item.getChildren(), false, projectId, noUpload);
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
        return itemFile.getName().matches("^[_a-zA-Z][a-zA-Z0-9_]*\\.xill$");
    }

    private boolean uploadRobot(final File robotFile, final File projectFolder, final boolean existCheck, String projectId, boolean noUpload) throws IOException {

        final String code;
        code = FileUtils.readFileToString(robotFile);

        final String robotFqn = xillServerUploader.getFqn(robotFile, projectFolder);
        if (projectId == null) {
            projectId = xillServerUploader.ensureProjectExist(xillServerUploader.getProjectName(projectFolder));
        }

        // Check if robot does not exceed the multipart file size limit
        if (robotFile.length() > maxFileSize) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Uploading robot",
                    "The robot cannot be uploaded to the Xill server.", String.format("The size of robot %1$s exceeds the server file size limit.", robotFqn),
                    ButtonType.CLOSE);
            dialog.showAndWait();
            return false;
        }

        if (noUpload) {
            return true;
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

        // Store robot's FQN to list for bulk validation process after entire uploading is done
        uploadedRobots.add(robotFqn);
        uploadedProjectId = projectId;

        return true;
    }

    private boolean uploadResource(final File resourceFile, final File projectFolder, final boolean existCheck, String projectId, boolean noUpload) throws IOException {
        final String resourceName = xillServerUploader.getResourceName(resourceFile, projectFolder);
        if (projectId == null) {
            projectId = xillServerUploader.ensureProjectExist(xillServerUploader.getProjectName(projectFolder));
        }

        // Check if resource does not exceed the multipart file size limit
        if (resourceFile.length() > maxFileSize) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Uploading resource",
                    "The resource cannot be uploaded to the Xill server.", String.format("The size of resource %1$s exceeds the server file size limit.", resourceName),
                    ButtonType.CLOSE);
            dialog.showAndWait();
            return false;
        }

        if (noUpload) {
            return true;
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
