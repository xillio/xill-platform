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

import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitCheckoutOperation extends GitOperation {
    private final String branch;

    /**
     * Create a new Git checkout operation.
     *
     * @param repo The repo to use.
     * @param branch The branch to check out.
     */
    public GitCheckoutOperation(JGitRepository repo, final String branch) {
        super(repo);
        this.branch = branch;
    }

    @Override
    protected void execute() throws GitAPIException {
        repo.checkout(branch);
    }
}
