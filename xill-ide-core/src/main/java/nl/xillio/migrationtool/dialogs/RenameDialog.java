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
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.XillEnvironment;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * A dialog to remove an item in the project view.
 */
public class RenameDialog extends FXMLDialog {
    private static final Logger LOGGER = Log.get();

    @FXML
    private TextField tfname;

    private final TreeItem<Pair<File, String>> treeItem;

    /**
     * Default constructor.
     *
     * @param treeItem the tree item on which the item will be deleted
     */
    public RenameDialog(final TreeItem<Pair<File, String>> treeItem) {
        super("/fxml/dialogs/Rename.fxml");
        this.treeItem = treeItem;
        setTitle("Rename");
        tfname.setText(this.treeItem.getValue().getKey().getName());
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        // Get the old file, new file name and new file.
        final File oldFile = treeItem.getValue().getKey();
        String fileName = tfname.getText();
        
        if (oldFile.isFile() && oldFile.toString().endsWith(XillEnvironment.ROBOT_EXTENSION) && !fileName.endsWith(XillEnvironment.ROBOT_EXTENSION)) {
            fileName += XillEnvironment.ROBOT_EXTENSION;
        }
        final File newFile = new File(oldFile.getParent(), fileName);
        try {
            // Rename the item and update the tree item.
            if (oldFile.isDirectory()) {
                FileUtils.moveDirectory(oldFile, newFile);
            } else {
                FileUtils.moveFile(oldFile, newFile);
            }
            treeItem.setValue(new Pair<>(newFile, tfname.getText()));
            close();
        } catch (IOException e) {
            LOGGER.error("IOException while renaming file.", e);
            new AlertDialog(AlertType.ERROR, "Failed to rename file/folder", "",
                    "Something went wrong while renaming a file/folder.\n" + e.getMessage()).showAndWait();
        }
    }
}
