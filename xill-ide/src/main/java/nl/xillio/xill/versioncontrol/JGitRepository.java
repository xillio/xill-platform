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
package nl.xillio.xill.versioncontrol;

import javafx.scene.control.Alert;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.GitAuthenticateDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link Repository} which uses the jGit library for interacting with Git repositories.
 * @author Daan Knoope
 */
public class JGitRepository implements Repository {
    private static final Logger LOGGER = Log.get();

    private Git repository;
    private CredentialsProvider credentials;

    public JGitRepository(File path) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder().addCeilingDirectory(path).findGitDir(path);

        try {
            repository = new Git(builder.build());
        } catch (IOException e) {
            LOGGER.error("An exception occurred while loading the repository.", e);
        } catch (IllegalArgumentException e) {
            //?
        }
    }

    @Override
    public void commit(String commitMessage) {
        try {
            repository.add().addFilepattern(".").call();
            repository.commit().setMessage(commitMessage).call();
        } catch (GitAPIException e) {
            showError("pushing", e.getMessage());
        }
    }

    @Override
    public void push() {
        tryCommand(repository.push(), "pushing");
    }

    @Override
    public void pull() {
        tryCommand(repository.pull(), "pulling");
    }

    @Override
    public boolean isInitialized() {
        return repository != null;
    }

    @Override
    public Set<String> getChangedFiles() {
        Set<String> changedFiles = new HashSet<>();
        try {
            Status status = repository.status().call();
            changedFiles.addAll(status.getUncommittedChanges()); // Add changes
            changedFiles.addAll(status.getUntracked()); // Add untracked files
        } catch (GitAPIException | NoWorkTreeException e) {
            LOGGER.error("Error retrieving changed files", e);
        }
        return changedFiles;
    }

    @Override
    public void setCredentials(String username, String password) {
        credentials = new UsernamePasswordCredentialsProvider(username, password);
    }

    private void setCredentialsProvider(TransportCommand command) {
        if (credentials != null) {
            command.setCredentialsProvider(credentials);
        }
    }

    private void tryCommand(TransportCommand cmd, String action) {
        setCredentialsProvider(cmd);

        // Try without authenticating.
        try {
            cmd.call();
            return;
        } catch (GitAPIException e) {
            // Ignore silently.
        }

        // Authenticate.
        new GitAuthenticateDialog(this).showAndWait();
        setCredentialsProvider(cmd);

        // Try again.
        try {
            cmd.call();
        } catch (GitAPIException e) {
            showError(action, e.getMessage());
        }
    }

    private void showError(String action, String message) {
        new AlertDialog(Alert.AlertType.ERROR, "Error while " + action, "An error occurred while " + action + ".", message).showAndWait();
    }
}
