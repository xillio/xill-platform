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

import me.biesaart.utils.Log;
import nl.xillio.xill.versioncontrol.GitException;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.slf4j.Logger;

/**
 * Git task to commit all changes and push to the repository.
 *
 * @author Edward van Egdom
 */
public class GitCommitAndPushOperation extends GitOperation {
    private static final Logger LOGGER = Log.get();

    private String commitMessage;
    private boolean committed;

    /**
     * Create a new commit and push operation.
     *
     * @param repo The repo to commit and push to.
     * @param commitMessage The commit message.
     */
    public GitCommitAndPushOperation(JGitRepository repo, String commitMessage) {
        super(repo);
        this.commitMessage = commitMessage;
    }

    @Override
    protected void execute() throws GitException {
        // Check if we already committed, otherwise multiple commits can be created.
        if (!committed) {
            repo.commitCommand(commitMessage);
            committed = true;
        }
        repo.pushCommand();
    }

    @Override
    protected void handleError(Throwable cause) {
        cancelOperation();
        super.handleError(cause);
    }

    @Override
    protected void cancelOperation() {
        try {
            repo.resetCommitCommand();
        } catch (GitException e) {
            LOGGER.error("Failed to revert commit.", e);
        }
    }
}
