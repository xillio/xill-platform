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
import javafx.scene.control.Label;
import nl.xillio.xill.versioncontrol.JGitRepository;
import nl.xillio.xill.versioncontrol.operations.GitPullOperation;

public class GitPullDialog extends GitDialog {
    @FXML
    protected Label confirmText;

    /**
     * Default constructor.
     *
     * @param repo the JGitRepository that will be pulled from.
     */
    public GitPullDialog(final JGitRepository repo) {
        super(repo, "/fxml/dialogs/GitPull.fxml");
        this.setTitle("Pull");
        confirmText.setText("Pull from repository: " + repo.getRepositoryName() + "?");
    }

    @FXML
    private void pullBtnPressed() {
        showProgress();

        GitPullOperation pull = new GitPullOperation(repo);
        pull.getThread().start();

        pull.setOnSucceeded(e -> setStatusToFinished());
    }
}

