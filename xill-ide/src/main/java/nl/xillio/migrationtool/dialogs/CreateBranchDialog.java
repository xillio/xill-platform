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
import javafx.scene.control.TextField;
import nl.xillio.xill.versioncontrol.GitException;
import nl.xillio.xill.versioncontrol.GitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

public class CreateBranchDialog extends FXMLDialog {
    @FXML
    private TextField name;

    private final GitRepository repo;

    public CreateBranchDialog(final GitRepository repo) {
        super("/fxml/dialogs/GitCreateBranch.fxml");
        setTitle("New branch");
        this.repo = repo;
    }

    @FXML
    private void okBtnPressed() {
        try {
            repo.createBranch(name.getText());
            repo.checkout(name.getText());
            close();
        } catch (GitException e) {
            new AlertDialog(Alert.AlertType.ERROR, "Error", "An error occurred while trying to create a new branch.", e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void cancelBtnPressed() {
        close();
    }
}
