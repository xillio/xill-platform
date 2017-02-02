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
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
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

    @Override
    public boolean isInitialized() {
        return repository != null;
    }

    @Override
    public void pushCommand() throws GitException {
        Iterable<PushResult> pushResults;
        try {
            pushResults = repository.push().setCredentialsProvider(auth.getCredentials()).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }

        // JGit doesn't automatically detect all issues with pushing, this manually checks if
        // the push operation succeeded.
        for(PushResult pushResult : pushResults) {
            for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates()) {
                if (remoteRefUpdate.getStatus() != RemoteRefUpdate.Status.OK) {
                    throw new GitException(String.format("Could not push updates to remote (error code: %s).",
                            remoteRefUpdate.getStatus()));
                }
            }
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
            MergeResult mergeResult = repository.pull().setCredentialsProvider(auth.getCredentials()).call().getMergeResult();

            MergeResult.MergeStatus mergeStatus = mergeResult.getMergeStatus();

            // Throw an error if merging, and therefore the pull operation, has failed
            // Exclude conflicting merge status, because we create a custom message for that
            if (mergeStatus != MergeResult.MergeStatus.CONFLICTING && !mergeStatus.isSuccessful()) {
                throw new GitException(String.format("Merge attempt was not successful (status: %s)",
                        mergeStatus.toString()));
            }

            // Return list of conflicts
            if (mergeResult.getConflicts() == null) {
                return Collections.emptySet();
            }

            return mergeResult.getConflicts().keySet();
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

    @Override
    public String getCurrentBranchName() {
        try {
            return repository.getRepository().getBranch();
        } catch (IOException e) {
            LOGGER.error("Exception while getting the branch name.", e);
        }
        return null;
    }

    @Override
    public void checkout(String branch) throws GitException {
        // If there is already a local branch, the branch should not be created.
        try {
            Set<String> localBranches = repository.branchList().call().stream().map(Ref::getName).map(this::friendlyBranchName).collect(Collectors.toSet());

        boolean exists = localBranches.contains(branch);

        // Checkout the branch, creating and tracking the remote if it does not exist yet.
        repository.checkout().setCreateBranch(!exists).setName(branch).setStartPoint("origin/" + branch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }
    }

    @Override
    public void createBranch(String name) throws GitException {
        try {
            repository.branchCreate().setName(name).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getChangedFiles() {
        Set<String> changes = new HashSet<>();
        try {
            Status status = repository.status().call();
            for (String fileName: status.getMissing() ) {
                changes.add("(removed)    " + fileName);
            }
            for (String fileName: status.getUntracked() ) {
                changes.add("(added)    " + fileName);
            }
            for (String fileName: status.getModified() ) {
                changes.add("(changed)    " + fileName);
            }
        } catch (GitAPIException | NoWorkTreeException e) {
            LOGGER.error("Error retrieving changed files.", e);
        }
        return changes;
    }

    public JGitAuth getAuth() {
        return auth;
    }

    @Override
    public String getRepositoryName() {
        return repository.getRepository().getDirectory().getParentFile().getName();
    }
}
