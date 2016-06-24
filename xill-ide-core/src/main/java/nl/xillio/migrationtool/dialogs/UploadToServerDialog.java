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
import javafx.scene.control.TreeItem;
import javafx.util.Pair;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.api.errors.NotImplementedException;

import java.io.File;

/**
 * A dialog to upload an item to the server.
 */
public class UploadToServerDialog extends FXMLDialog {

    private final ObservableList<TreeItem<Pair<File, String>>> treeItems;
    private final ProjectPane projectPane;

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
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void okayBtnPressed(final ActionEvent event) {
        throw new NotImplementedException("Upload to server functionality has not yet been implemented.");
    }

}
