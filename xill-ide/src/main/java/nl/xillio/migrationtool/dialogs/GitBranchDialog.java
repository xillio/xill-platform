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
import nl.xillio.xill.versioncontrol.JGitRepository;
import nl.xillio.xill.versioncontrol.operations.GitCheckoutOperation;

import java.util.List;

public class GitBranchDialog extends GitDialog {
    @FXML
    private ListView<String> lvBranches;

    public GitBranchDialog(final JGitRepository repo) {
        super(repo, "/fxml/dialogs/GitBranchList.fxml");
        setTitle("Branches");
        getBranchList();
    }

    private void getBranchList() {
        lvBranches.getItems().clear();
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

        // Check if we are trying to check out the current branch, which is always the first item.
        if (lvBranches.getSelectionModel().getSelectedIndex() == 0) {
            return;
        }

        showProgress();
        GitCheckoutOperation checkout = new GitCheckoutOperation(repo, branch);
        checkout.setOnSucceeded(e -> setStatusToFinished());
        checkout.getThread().start();
    }

    @FXML
    private void createBtnPressed() {
        new CreateBranchDialog(repo).showAndWait();
        getBranchList();
    }

    @FXML
    private void closeBtnPressed() {
        close();
    }
}
