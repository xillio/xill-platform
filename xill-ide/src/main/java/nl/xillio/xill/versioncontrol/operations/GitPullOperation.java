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
package nl.xillio.xill.versioncontrol.operations;

import javafx.application.Platform;
import nl.xillio.migrationtool.dialogs.GitConflictDialog;
import nl.xillio.xill.versioncontrol.GitException;
import nl.xillio.xill.versioncontrol.JGitRepository;

import java.util.Set;

/**
 * Git pull task
 *
 * @author Edward van Egdom
 */
public class GitPullOperation extends GitOperation {
    /**
     * Create a new Git pull operation.
     *
     * @param repo The repo to pull from.
     */
    public GitPullOperation(JGitRepository repo) {
        super(repo);
    }

    @Override
    @SuppressWarnings("squid:S1612") // Lambda cannot be converted to method reference because it causes JavaFX to throw errors
    protected void execute() throws GitException {
        Set<String> conflicts = repo.pullCommand();

        if (conflicts.size() > 0) {
            Platform.runLater(() -> new GitConflictDialog(conflicts).showAndWait());
        }
    }
}
