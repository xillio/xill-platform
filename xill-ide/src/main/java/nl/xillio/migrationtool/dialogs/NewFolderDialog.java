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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import nl.xillio.migrationtool.gui.ProjectPane;
import org.apache.commons.io.FileUtils;
import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * A dialog for creating a new folder in the project view.
 */
public class NewFolderDialog extends FXMLDialog {

    @FXML
    private TextField tffolder;

    private final TreeItem<Pair<File, String>> treeItem;
    private final ProjectPane projectPane;

    private static final Logger LOGGER = Log.get();

    /**
     * Default constructor.
     *
     * @param projectPane the projectPane to which this dialog is attached to.
     * @param treeItem    the tree item on which the item will be deleted
     */
    public NewFolderDialog(final ProjectPane projectPane, final TreeItem<Pair<File, String>> treeItem) {
        super("/fxml/dialogs/NewFolder.fxml");
        this.setTitle("Add Folder");
        this.projectPane = projectPane;
        this.treeItem = treeItem;

        // If the folder to create the new folder in does not exist, create it.
        projectPane.makeDirIfNotExists(treeItem);
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        try {
            File newFolder = new File(getFolder(treeItem.getValue().getKey()), tffolder.getText());
            FileUtils.forceMkdir(newFolder);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            AlertDialog error = new AlertDialog(AlertType.ERROR, "Could not create folder", "",
                    e.getMessage(), ButtonType.OK);
            error.show();
        }
        close();
    }

    private File getFolder(File key) {
        return key.isDirectory() ? key : key.getParentFile();
    }
}
