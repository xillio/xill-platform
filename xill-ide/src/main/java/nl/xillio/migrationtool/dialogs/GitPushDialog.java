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
import nl.xillio.xill.versioncontrol.commands.GitCommitAndPush;

import java.util.Set;

public class GitPushDialog extends FXMLDialog {
    @FXML
    private TextField message;
    @FXML
    private ListView<String> fileList;
    @FXML
    private Button okBtn;

    @FXML
    private HBox progress;

    @FXML
    private Label messageStatus;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private VBox gitInfoBox;

    private final JGitRepository repo;

    /**
     * Default constructor.
     *
     * @param repo the JGitRepository that will be pushed to.
     */
    public GitPushDialog(final JGitRepository repo) {
        super("/fxml/dialogs/GitPush.fxml");
        this.setTitle("Push changes");
        this.repo = repo;

        Set<String> changedFiles = repo.getChangedFiles();
        if (!changedFiles.isEmpty()) {
            fileList.setItems(FXCollections.observableArrayList(changedFiles));
        } else {
            fileList.setItems(FXCollections.observableArrayList("No changes were found"));
            okBtn.setDisable(true);
        }
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void pushBtnPressed(final ActionEvent event) {
        progressIndicator.setVisible(true);
        progress.setVisible(true);
        gitInfoBox.setDisable(true);

        GitCommitAndPush push = new GitCommitAndPush(repo, message.getText());
        new Thread(push).start();

        push.setOnSucceeded(e -> setStatusToFinished());
    }

    private void setStatusToFinished() {
        System.out.println("Finished!");
        progressIndicator.setVisible(false);
        messageStatus.setText("Successfully pushed changes");
    }
}

