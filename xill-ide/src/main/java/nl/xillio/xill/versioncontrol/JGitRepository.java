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

import me.biesaart.utils.Log;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementation of {@link GitRepository} which uses the jGit library for interacting with Git repositories.
 * @author Daan Knoope
 */
public class JGitRepository implements GitRepository {
    private static final Logger LOGGER = Log.get();

    private Git repository;
    private JGitAuth auth;

    public JGitRepository(File path) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder().addCeilingDirectory(path).findGitDir(path);

        try {
            if (builder.getGitDir() != null) {
                repository = new Git(builder.build());
            }
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.error("An exception occurred while loading the repository.", e);
        }

        auth = new JGitAuth();
    }

    @Override
    public boolean isInitialized() {
        return repository != null;
    }

    @Override
    public void pushCommand() throws GitException {
        try {
            repository.push().setCredentialsProvider(auth.getCredentials()).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }

    }

    @Override
    public void commitCommand(String message) throws GitException {
        try {
            repository.add().addFilepattern(".").call();
            repository.add().setUpdate(true).addFilepattern(".").call();
            repository.commit().setMessage(message).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> pullCommand() throws GitException {
        try {
            MergeResult mr = repository.pull().setCredentialsProvider(auth.getCredentials()).call().getMergeResult();
            if (mr.getConflicts() == null) {
                return null;
            }
            return mr.getConflicts().keySet();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }
    }

    @Override
    public void resetCommitCommand() throws GitException {
        try {
            repository.reset().setMode(ResetCommand.ResetType.MIXED).setRef("HEAD^").call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }
    }

    @Override
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

    public JGitAuth getAuth() {
        return auth;
    }

    @Override
    public String getRepositoryName() {
        return repository.getRepository().getDirectory().getParentFile().getName();
    }
}
