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
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import nl.xillio.xill.versioncontrol.Repository;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.List;

public class BranchListDialog extends FXMLDialog {
    @FXML
    private ListView<String> lvBranches;

    private final Repository repo;

    public BranchListDialog(final Repository repo) {
        super("/fxml/dialogs/BranchList.fxml");
        setTitle("Branches");
        this.repo = repo;
        getBranchList();
    }

    private void getBranchList() {
        List<String> branches = repo.getBranches();
        String current = repo.getCurrentBranchName();

        // Move the current branch to the top and prefix it with an asterisk.
        branches.remove(current);
        branches.add(0, "* " + current);

        lvBranches.getItems().setAll(branches);
    }

    @FXML
    private void checkoutBtnPressed() {
        String branch = lvBranches.getSelectionModel().getSelectedItem();
        try {
            repo.checkout(branch);
        } catch (GitAPIException e) {
            new AlertDialog(Alert.AlertType.ERROR, "Error", "An error occurred while checking out " + branch + ".", e.getMessage()).showAndWait();
        }
        close();
    }

    @FXML
    private void createBtnPressed() {
        new CreateBranchDialog(repo).showAndWait();
        close();
    }

    @FXML
    private void closeBtnPressed() {
        close();
    }
}
