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

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;

public class BranchListDialog extends FXMLDialog {
    @FXML
    private ListView<String> lvBranches;

    public BranchListDialog(List<String> branches, String currentBranch) {
        super("/fxml/dialogs/BranchList.fxml");
        setTitle("Branches");

        // Set the branch list.
        lvBranches.getItems().setAll(branches);
        lvBranches.getSelectionModel().select(currentBranch);
    }

    @FXML
    private void closeBtnPressed() {
        close();
    }
}
