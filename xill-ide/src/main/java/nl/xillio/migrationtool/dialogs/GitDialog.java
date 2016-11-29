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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.biesaart.utils.Log;
import nl.xillio.xill.versioncontrol.JGitRepository;
import nl.xillio.xill.versioncontrol.operations.GitOperation;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Created by Dwight on 23-Nov-16.
 */
public abstract class GitDialog extends FXMLDialog {

    protected final JGitRepository repo;
    protected final static String GitDialogDescription = "/fxml/dialogs/GitDialog.fxml";
    private static final Logger LOGGER = Log.get();

    @FXML
    protected Button okBtn;
    @FXML
    protected HBox progress;
    @FXML
    protected VBox componentsContainer;


    /**
     * Default constructor.
     *
     * @param repo the JGitRepository that will be pushed to.
     */
    public GitDialog(final JGitRepository repo, final String action, String componentsDescription) {
        super(GitDialogDescription);
        this.repo = repo;
        this.setTitle(action);
        okBtn.setText(action);
        addDialogComponents(componentsDescription);

    }

    protected void addDialogComponents(String componentsDescription){
        FXMLLoader loader = new FXMLLoader(getClass().getResource(componentsDescription));
        loader.setController(this);
        try {
            Parent components = loader.load();
            componentsContainer.getChildren().add(0,components);
        } catch (IOException e) {
            LOGGER.error("Could not load components for Git dialog.");
        }
    }

    @FXML
    protected void cancelBtnPressed() {
        close();
    }

    protected void startProgress(GitOperation operation) {
        progress.setVisible(true);
        okBtn.setDisable(true);

        operation.setOnSucceeded(e -> setStatusToFinished());
        operation.getThread().start();
    }

    protected void setStatusToFinished() {
        this.close();
    }

    @FXML
    protected abstract void actionBtnPressed();
}
