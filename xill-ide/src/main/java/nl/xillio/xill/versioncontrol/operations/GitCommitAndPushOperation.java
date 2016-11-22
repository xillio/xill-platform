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
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

/**
 * Git task to commit all changes and push to the repository.
 *
 * @author Edward van Egdom
 */
public class GitCommitAndPushOperation extends GitOperation {
    private String commitMessage;

    private static final Logger LOGGER = Log.get();

    public GitCommitAndPushOperation(JGitRepository repo, String commitMessage) {
        super(repo);
        this.commitMessage = commitMessage;
    }

    @Override
    public void execute() throws GitAPIException {
        repo.commitCommand(commitMessage);
        repo.pushCommand();
    }

    @Override
    public void handleError(Throwable cause) {
        revertCommit();
        super.handleError(cause);
    }

    private void revertCommit() {
        try {
            repo.resetCommitCommand();
        } catch (GitAPIException e) {
            LOGGER.error("Failed to revert commit", e);
        }
    }
}
