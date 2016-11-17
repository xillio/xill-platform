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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link Repository} which uses the jGit library for interacting with Git repositories.
 *
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
        }
    }

    @Override
    public boolean commit(String commitMessage) {
        try {
            repository.add().addFilepattern("--all").call();
            repository.commit().setMessage(commitMessage).call();
        } catch (GitAPIException e) {
            LOGGER.error("Exception while committing files.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean push() {
        try {
            PushCommand cmd = repository.push();
            setCredentialsProvider(cmd);
            cmd.call();
        } catch (GitAPIException e) {
            LOGGER.error("Exception while pushing.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean pull() {
        try {
            PullCommand cmd = repository.pull();
            setCredentialsProvider(cmd);
            cmd.call();
        } catch (GitAPIException e) {
            LOGGER.error("Exception while pulling.", e);
            return false;
        }
        return true;
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
    public void checkout(String branch) throws GitAPIException {
        // If there is already a local branch, the branch should not be created.
        Set<String> localBranches = repository.branchList().call().stream().map(Ref::getName).map(this::friendlyBranchName).collect(Collectors.toSet());
        boolean exists = localBranches.contains(branch);

        // Checkout the branch, creating and tracking the remote if it does not exist yet.
        repository.checkout().setCreateBranch(!exists).setName(branch).setStartPoint("origin/" + branch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
    }

    @Override
    public void createBranch(String name) throws GitAPIException {
        repository.branchCreate().setName(name).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
    }

    @Override
    public boolean isInitialized() {
        return repository != null;
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
}
