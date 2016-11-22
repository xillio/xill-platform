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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nl.xillio.xill.versioncontrol.JGitRepository;
import nl.xillio.xill.versioncontrol.operations.GitPullOperation;

public class GitPullDialog extends FXMLDialog {
    @FXML
    private TextField message;
    @FXML
    private Label repositoryName;
    @FXML
    private Button okBtn;

    @FXML
    private HBox progress;

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
    public GitPullDialog(final JGitRepository repo) {
        super("/fxml/dialogs/GitPull.fxml");
        this.setTitle("Pull changes");
        this.repo = repo;
        repositoryName.setText(repo.getRepositoryName());
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

        GitPullOperation pull = new GitPullOperation(repo);
        new Thread(pull).start();

        pull.setOnSucceeded(e -> setStatusToFinished());
    }

    private void setStatusToFinished() {
        progressIndicator.setVisible(false);
        this.close();
    }
}

