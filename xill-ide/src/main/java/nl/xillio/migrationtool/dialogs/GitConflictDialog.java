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

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.Set;

public class GitConflictDialog extends FXMLDialog {
    @FXML
    private ListView fileList;
    @FXML
    private Label message;

    /**
     * Default constructor.
     *
     * @param conflicts The set containing the names of all conflicted files.
     */
    public GitConflictDialog(final Set<String> conflicts) {
        super("/fxml/dialogs/GitConflict.fxml");
        this.setTitle("Conflicts");
        fileList.setItems(FXCollections.observableArrayList(conflicts));
    }

    @FXML
    private void okBtnPressed(final ActionEvent event) {
        close();
    }
}