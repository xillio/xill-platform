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
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import nl.xillio.xill.versioncontrol.JGitRepository;

import java.util.Set;

public class GitPushDialog extends FXMLDialog {
    @FXML
    private TextField message;
    @FXML
    private ListView<String> fileList;

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
        }
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void pushBtnPressed(final ActionEvent event) {
        repo.commit(message.getText());
        repo.push();
        close();
    }
}

