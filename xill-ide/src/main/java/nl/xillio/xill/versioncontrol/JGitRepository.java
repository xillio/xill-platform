/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.versioncontrol;

import me.biesaart.utils.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link Repository} which uses the jGit library for interacting with Git repositories.
 * @author Daan Knoope
 */
public class JGitRepository {
    private static final Logger LOGGER = Log.get();

    private Git repository;

    private JGitAuth auth;

    public JGitRepository(File path) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder().addCeilingDirectory(path).findGitDir(path);

        try {
            repository = new Git(builder.build());
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error("An exception occurred while loading the repository.", e);
        }

        auth = new JGitAuth();
    }

    public boolean isInitialized() {
        return repository != null;
    }

    public JGitAuth getAuth() {
        return auth;
    }

    public String getRepositoryName() {
        return repository.getRepository().getDirectory().getParentFile().getName();
    }

    /* Edward's commands */
    public void pushCommand() throws GitAPIException {
        repository.push().setCredentialsProvider(auth.getCredentials()).call();
    }

    public void commitCommand(String message) throws GitAPIException {
        repository.add().addFilepattern(".").call();
        repository.commit().setMessage(message).call();
    }

    public void pullCommand() throws GitAPIException {
        repository.pull().setCredentialsProvider(auth.getCredentials()).call();
    }
    /* End */

    private void resetCommit() {
        try {
            repository.reset().setMode(ResetCommand.ResetType.SOFT).setRef("HEAD^").call();
        } catch (GitAPIException e) {
            LOGGER.error("Error resetting commit.", e);
        }
    }

    public Set<String> getChangedFiles() {
        Set<String> changedFiles = new HashSet<>();
        try {
            Status status = repository.status().call();
            changedFiles.addAll(status.getUncommittedChanges()); // Add changes
            changedFiles.addAll(status.getUntracked()); // Add untracked files
        } catch (GitAPIException | NoWorkTreeException e) {
            LOGGER.error("Error retrieving changed files.", e);
        }
        return changedFiles;
    }
}
