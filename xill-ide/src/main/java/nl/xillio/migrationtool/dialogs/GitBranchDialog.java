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
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import nl.xillio.xill.versioncontrol.JGitRepository;
import nl.xillio.xill.versioncontrol.operations.GitCheckoutOperation;

import java.util.List;

public class GitBranchDialog extends GitDialog {
    @FXML
    private ListView<Text> lvBranches;

    @FXML
    private Label messageStatus;

    private static Font normalFont = Font.getDefault();
    private static Font boldFont = Font.font(normalFont.getFamily(), FontWeight.BOLD, normalFont.getSize());

    public GitBranchDialog(final JGitRepository repo) {
        super(repo, "Checkout branch", "/fxml/dialogs/GitBranchList.fxml");
        getBranchList();
    }

    private void getBranchList() {
        lvBranches.getItems().clear();

        List<String> branches = repo.getBranches();
        String currentBranch = repo.getCurrentBranchName();
        ObservableList<Text> branchList = FXCollections.observableArrayList();

        // Place current branch at the top of the list
        Text currentBranchText = new Text(currentBranch);
        currentBranchText.setFont(boldFont);
        branchList.add(0, currentBranchText);

        for(String branch : branches){
            if(!branch.equals(currentBranch)) {
                Text t = new Text(branch);
                t.setFont(normalFont);
                branchList.add(t);
            }
        }

        lvBranches.setItems(branchList);
        okBtn.setDisable(true);
        if (lvBranches.getItems().size() > 1) {
            lvBranches.getSelectionModel().selectedItemProperty().addListener((obs) -> okBtn.setDisable(lvBranches.getSelectionModel().getSelectedIndex() == 0));
        }
    }

    @Override
    @FXML
    protected void actionBtnPressed() {
        // Check if we are trying to check out the current branch, which is always the first item.
        if (lvBranches.getSelectionModel().getSelectedIndex() == 0) {
            return;
        }

        String branch = lvBranches.getSelectionModel().getSelectedItem().getText();

        messageStatus.textProperty().setValue("Checking out branch...");
        startProgress(new GitCheckoutOperation(repo, branch));
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
