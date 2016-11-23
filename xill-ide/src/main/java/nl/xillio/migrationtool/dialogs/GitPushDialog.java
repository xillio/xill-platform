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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nl.xillio.xill.versioncontrol.JGitRepository;
import nl.xillio.xill.versioncontrol.operations.GitCommitAndPushOperation;

import java.util.Set;

public class GitPushDialog extends GitDialog {
    @FXML
    private TextField message;
    @FXML
    private ListView<String> fileList;

    @FXML
    private Label messageStatus;

    @FXML
    private VBox gitInfoBox;

    /**
     * Default constructor.
     *
     * @param repo the JGitRepository that will be pushed to.
     */
    public GitPushDialog(final JGitRepository repo) {
        super(repo, "/fxml/dialogs/GitPush.fxml");
        this.setTitle("Push changes");

        okBtn.setDisable(true);
        Set<String> changedFiles = repo.getChangedFiles();
        boolean changes = !changedFiles.isEmpty();

        if (changes) {
            fileList.setItems(FXCollections.observableArrayList(changedFiles));
            message.textProperty().addListener((observable, newValue, oldValue) -> {
                if(!(newValue.toString().equals(""))){
                    okBtn.setDisable(false);
                }
                else{
                    okBtn.setDisable(true);
                }
            });
        } else {
            fileList.setItems(FXCollections.observableArrayList("No changes were found"));
        }
    }

    @FXML
    private void pushBtnPressed(final ActionEvent event) {
        showProgress();
        gitInfoBox.setDisable(true);

        GitCommitAndPushOperation push = new GitCommitAndPushOperation(repo, message.getText());
        new Thread(push).start();

        push.setOnSucceeded(e -> setStatusToFinished());
    }
}

