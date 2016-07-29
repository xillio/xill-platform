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
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import me.biesaart.utils.Log;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;

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

        // If the file is renamed without extension, add the old extension.
        String oldExtension = FilenameUtils.getExtension(oldFile.toString());
        if (oldFile.isFile() && FilenameUtils.getExtension(fileName).isEmpty()) {
            fileName += "." + oldExtension;
        }

        final File newFile = new File(oldFile.getParent(), fileName);
        if (!oldFile.renameTo(newFile)) {
            new AlertDialog(Alert.AlertType.ERROR, "Failed to rename file/folder", "",
                    "Could not rename file " + oldFile.getName() + " to " + newFile.getName()).showAndWait();
        } else {
            treeItem.setValue(new Pair<>(newFile, tfname.getText()));
            close();
        }
    }
}
