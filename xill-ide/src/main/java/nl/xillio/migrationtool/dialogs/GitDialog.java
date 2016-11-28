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
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import nl.xillio.xill.versioncontrol.JGitRepository;

/**
 * Created by Dwight on 23-Nov-16.
 */
public class GitDialog extends FXMLDialog {
    @FXML
    protected Button okBtn;
    @FXML
    protected HBox progress;

    protected final JGitRepository repo;

    /**
     * Default constructor.
     *
     * @param repo the JGitRepository that will be pushed to.
     */
    public GitDialog(final JGitRepository repo, final String path) {
        super(path);
        this.repo = repo;
    }

    @FXML
    protected void cancelBtnPressed() {
        close();
    }

    protected void showProgress() {
        progress.setVisible(true);
    }

    protected void setStatusToFinished() {
        this.close();
    }
}
