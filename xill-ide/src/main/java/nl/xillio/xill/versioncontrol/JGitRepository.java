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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GitRepository} which uses the jGit library for interacting with Git repositories.
 *
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

    public boolean isInitialized() {
        return repository != null;
    }

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

    public void resetCommitCommand() throws GitAPIException {
        repository.reset().setMode(ResetCommand.ResetType.MIXED).setRef("HEAD^").call();
    }

    public List<String> getBranches() {
        try {
            // Get all remote branches.
            List<Ref> remotes = repository.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            return remotes.stream().map(Ref::getName).map(this::friendlyBranchName).distinct().collect(Collectors.toList());
        } catch (GitAPIException e) {
            LOGGER.error("Exception while getting branches.", e);
            return Collections.emptyList();
        }
    }

    private String friendlyBranchName(String name) {
        return name.substring(name.lastIndexOf('/') + 1);
    }

    public String getCurrentBranchName() {
        try {
            return repository.getRepository().getBranch();
        } catch (IOException e) {
            LOGGER.error("Exception while getting the branch name.", e);
        }
        return null;
    }

    public void checkout(String branch) throws GitAPIException {
        // If there is already a local branch, the branch should not be created.
        Set<String> localBranches = repository.branchList().call().stream().map(Ref::getName).map(this::friendlyBranchName).collect(Collectors.toSet());
        boolean exists = localBranches.contains(branch);

        // Checkout the branch, creating and tracking the remote if it does not exist yet.
        repository.checkout().setCreateBranch(!exists).setName(branch).setStartPoint("origin/" + branch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
    }

    public void createBranch(String name) throws GitAPIException {
        repository.branchCreate().setName(name).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
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

    public JGitAuth getAuth() {
        return auth;
    }

    public String getRepositoryName() {
        return repository.getRepository().getDirectory().getParentFile().getName();
    }
}
