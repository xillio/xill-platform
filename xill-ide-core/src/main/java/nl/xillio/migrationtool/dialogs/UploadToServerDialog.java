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
import javafx.scene.layout.GridPane;
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
import java.util.Optional;
import java.util.stream.Collectors;

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
    private boolean overwriteAll = false;

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
            overwriteAll = false;

            // Get current server settings
            queryServerSettings();

            // Assign projectId
            String projectId = null;
            if(treeItems.size() > 0) {
                projectId = ensureProjectExists(treeItems.get(0));
            }

            // Process items (do selected robots and resources upload)
            if (!processItems(treeItems, projectId)) {
                return; // Process has been user interrupted - so no success dialog is shown
            }

            if (!invalidRobots.isEmpty()) { // Some robots have invalid name
                InvalidRobotNamesDialog dialog = new InvalidRobotNamesDialog(StringUtils.join(invalidRobots.toArray(), '\n'));
                dialog.showAndWait();
            }

            // Validate uploaded robots (in a one bulk server request - because of every Xill environment start is very time consuming)
            if (cbvalidate.isSelected() && !uploadedRobots.isEmpty()) {
                xillServerUploader.validateRobots(uploadedProjectId, uploadedRobots);
            }

            // Show success message
            AlertDialog dialog = new AlertDialog(Alert.AlertType.INFORMATION, "Upload to server",
                    "Uploading process has been successfully finished.", "",
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

    private boolean isProject(final TreeItem<Pair<File, String>> item) {
        return item.getParent() == projectPane.getRoot();
    }

    private boolean isFolder(final TreeItem<Pair<File, String>> item) {
        return item.getValue().getKey().isDirectory();
    }

    private enum ExistsResult {
        NO,
        YES_OVERWRITE,
        YES_CANCEL
    }

    private ExistsResult resourceExists(final File resourceFile, String projectId, final File projectFolder) throws IOException {
        final String resourceName = xillServerUploader.getResourceName(resourceFile, projectFolder);

        if (xillServerUploader.existResource(projectId, resourceName)) {
            if(overwriteAll){
                return ExistsResult.YES_OVERWRITE;
            }

            // Set up dialog content
            CheckBox checkBox = new CheckBox("Overwrite all");
            GridPane gridPane = new GridPane();
            GridPane.setMargin(checkBox, new javafx.geometry.Insets(20, 0, 0, 0));
            gridPane.add(checkBox, 0, 1);

            ContentAlertDialog dialog = new ContentAlertDialog(Alert.AlertType.WARNING, "Uploading resource",
                    String.format("The resource %1$s already exists on the server", resourceName),
                    "Do you want to overwrite it?",
                    gridPane,
                    ButtonType.YES, ButtonType.NO);
            dialog.showAndWait();

            final Optional<ButtonType> result = dialog.getResult();
            if (result.get().getButtonData() != ButtonBar.ButtonData.YES) {
                return ExistsResult.YES_CANCEL;
            }

            overwriteAll = checkBox.isSelected();
            return ExistsResult.YES_OVERWRITE;
        }

        return ExistsResult.NO;
    }

    private ExistsResult projectExists(final TreeItem<Pair<File, String>> item) throws IOException {
        final File projectFolder = projectPane.getProject(item).getValue().getKey();
        final String projectName = xillServerUploader.getProjectName(projectFolder);

        if (xillServerUploader.findProject(projectName) != null) {
            // Set up dialog content
            CheckBox checkBox = new CheckBox("Overwrite all");
            GridPane gridPane = new GridPane();
            GridPane.setMargin(checkBox, new javafx.geometry.Insets(20, 0, 0, 0));
            gridPane.add(checkBox, 0, 1);

            ContentAlertDialog dialog = new ContentAlertDialog(Alert.AlertType.WARNING, "Uploading project",
                    String.format("The project %1$s already exists on the server", projectName),
                    "Do you want to overwrite entire project?",
                    gridPane,
                    ButtonType.YES, ButtonType.NO);

            dialog.showAndWait();
            final Optional<ButtonType> result = dialog.getResult();

            if (result.get().getButtonData() != ButtonBar.ButtonData.YES) {
                return ExistsResult.YES_CANCEL;
            }

            overwriteAll = checkBox.isSelected();
            return ExistsResult.YES_OVERWRITE;
        }
        return ExistsResult.NO;
    }

    private ExistsResult robotExists(final File robotFile, String projectId, final File projectFolder) throws IOException {
        final String robotFqn = xillServerUploader.getFqn(robotFile, projectFolder);
        if (projectId == null) {
            projectId = xillServerUploader.ensureProjectExist(xillServerUploader.getProjectName(projectFolder));
        }

        if(xillServerUploader.existRobot(projectId, robotFqn)) {
            if(overwriteAll){
                return ExistsResult.YES_OVERWRITE;
            }

            // Set up dialog content
            CheckBox checkBox = new CheckBox("Overwrite all");
            GridPane gridPane = new GridPane();
            GridPane.setMargin(checkBox, new javafx.geometry.Insets(20, 0, 0, 0));
            gridPane.add(checkBox, 0, 1);

            // Set up dialog
            ContentAlertDialog dialog = new ContentAlertDialog(Alert.AlertType.WARNING, "Uploading robot",
                    String.format("The robot %1$s already exists on the server", robotFile.getName()),
                    "Do you want to overwrite it?",
                    gridPane,
                    ButtonType.YES, ButtonType.NO);

            dialog.showAndWait();
            final Optional<ButtonType> result = dialog.getResult();

            if (result.get().getButtonData() != ButtonBar.ButtonData.YES) {
                return ExistsResult.YES_CANCEL;
            }

            overwriteAll = checkBox.isSelected();
            return ExistsResult.YES_OVERWRITE;
        }

        xillServerUploader.existRobot(projectId, robotFqn);

        return ExistsResult.NO;
    }

    private String ensureProjectExists(final TreeItem<Pair<File, String>> item) throws IOException {
        final File projectFolder = projectPane.getProject(item).getValue().getKey();
        return xillServerUploader.ensureProjectExist(xillServerUploader.getProjectName(projectFolder));
    }

    private void uploadChildren(final TreeItem<Pair<File, String>> item, String projectId) throws IOException {
        for (TreeItem<Pair<File, String>> subItem : item.getChildren()) {
            if (isFolder(subItem)) {
                uploadFolder(subItem, projectId);
                continue;
            }

            handleFileUploading(subItem, projectId);
        }
    }

    private void uploadFolder(final TreeItem<Pair<File, String>> item, String projectId) throws IOException {
        uploadChildren(item, projectId);
    }

    private void uploadProject(final TreeItem<Pair<File, String>> item, String projectId) throws IOException {
        uploadChildren(item, projectId);
    }

    private void uploadRobot(final File robotFile, final File projectFolder, String projectId) throws IOException  {
        // Do test for invalid robot name
        if (isInvalidRobotFile(robotFile)) {
            invalidRobots.add(xillServerUploader.getFqn(robotFile, projectFolder));
        }

        // Check if robot does not exceed the multipart file size limit
        if (robotFile.length() > maxFileSize) {
            final String robotFqn = xillServerUploader.getFqn(robotFile, projectFolder);

            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Uploading robot",
                    "The robot cannot be uploaded to the Xill server.",
                    String.format("The size of robot %1$s exceeds the server file size limit.", robotFqn),
                    ButtonType.CLOSE);
            dialog.showAndWait();
            return;
        }

        final String code = FileUtils.readFileToString(robotFile);
        final String robotFqn = xillServerUploader.getFqn(robotFile, projectFolder);

        // Upload the robot
        xillServerUploader.uploadRobot(projectId, robotFqn, code);

        // Store robot's FQN to list for bulk validation process after entire uploading is done
        uploadedRobots.add(robotFqn);
        uploadedProjectId = projectId;
    }

    private void uploadResource(final File resourceFile, final File projectFolder, String projectId) throws IOException  {
        final String resourceName = xillServerUploader.getResourceName(resourceFile, projectFolder);

        // Check if resource does not exceed the multipart file size limit
        if (resourceFile.length() > maxFileSize) {
            AlertDialog dialog = new AlertDialog(Alert.AlertType.ERROR, "Uploading resource",
                    "The resource cannot be uploaded to the Xill server.",
                    String.format("The size of resource %1$s exceeds the server file size limit.", resourceName),
                    ButtonType.CLOSE);
            dialog.showAndWait();
            return;
        }

        // Upload the resource
        xillServerUploader.uploadResource(projectId, resourceName, resourceFile);
    }

    private boolean handleProjectUploading(TreeItem<Pair<File, String>> item, String projectId) throws IOException {
        ExistsResult existsResult = projectExists(item);

        if (existsResult == ExistsResult.YES_CANCEL) {
            return false;
        } else if (existsResult == ExistsResult.YES_OVERWRITE) {
            xillServerUploader.deleteProject(projectId);
        }

        projectId = ensureProjectExists(item);
        uploadProject(item, projectId);
        return true;
    }

    private boolean handleFileUploading(TreeItem<Pair<File, String>> item, String projectId) throws IOException {
        ExistsResult existsResult;
        final File projectFolder = projectPane.getProject(item).getValue().getKey();
        final File itemFile = item.getValue().getKey();

        if(isRobot(itemFile, projectFolder)) {
            existsResult = robotExists(item.getValue().getKey(), projectId, projectFolder);
            if(existsResult == ExistsResult.YES_CANCEL) {
                return false;
            }

            uploadRobot(itemFile, projectFolder, projectId);
        } else { //is resource
            existsResult = resourceExists(item.getValue().getKey(), projectId, projectFolder);
            if(existsResult == ExistsResult.YES_CANCEL) {
                return false;
            }

            uploadResource(itemFile, projectFolder, projectId);
        }
        return true;
    }

    private boolean handleFolderUploading(TreeItem<Pair<File, String>> item, String projectId) throws IOException {
        uploadFolder(item, projectId);
        return true;
    }

    private boolean processItems(final List<TreeItem<Pair<File, String>>> items, String projectId) throws IOException {
        for (TreeItem<Pair<File, String>> item : items) {
            if (isProject(item)) {
                if(!handleProjectUploading(item, projectId)) {
                    return false;
                }
            } else if (isFolder(item)) {// Directory
                if(!handleFolderUploading(item, projectId)) {
                    return false;
                }
            } else {
                if(!handleFileUploading(item, projectId)) {
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

    private boolean isInvalidRobotFile(final File itemFile) {
        final boolean isXill = itemFile.getName().matches("^.*\\.xill$"); //it is a robot
        final boolean validName = itemFile.getName().matches("^[_a-zA-Z][a-zA-Z0-9_]*\\.xill$"); //name is valid
        return (isXill && !validName);
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
}
