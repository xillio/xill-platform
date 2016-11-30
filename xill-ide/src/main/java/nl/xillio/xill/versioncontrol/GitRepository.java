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

import org.eclipse.jgit.api.PullResult;

import java.util.List;
import java.util.Set;

/**
 * Representation of a Git version control repository.
 *
 * @author Daan Knoope
 */
public interface GitRepository {
    /**
     * Commits all changes with the provided commit message.
     *
     * @param commitMessage The message for the commit.
     * @return {@code true} if commit succeeded, {@code false} otherwise.
     */
    void commitCommand(String commitMessage) throws GitException;

    /**
     * Pushes all changes to the remote repository.
     */
    void pushCommand() throws GitException;

    /**
     * Pulls all changes from the remote repository.
     */
    Set<String> pullCommand() throws GitException;

    /**
     * Resets the last commit.
     */
    void resetCommitCommand() throws GitException;

    /**
     * Get the branches on this repo.
     *
     * @return The list of branches.
     */
    List<String> getBranches();

    /**
     * Get the current branch name.
     *
     * @return The current branch name.
     */
    String getCurrentBranchName();

    /**
     * Check out a branch.
     *
     * @param branch The branch to check out.
     */
    void checkout(String branch) throws GitException;

    /**
     * Create a new branch.
     *
     * @param name The name of the branch to create.
     */
    void createBranch(String name) throws GitException;

    /**
     * Returns if this object exists as initialized repository on the disk.
     *
     * @return {@code true} if this object is initialized on the disk, {@code false} otherwise.
     */
    boolean isInitialized();

    /**
     * Returns a set of files that have been changed in this working copy
     *
     * @return A set of file names
     */
    Set<String> getChangedFiles();

    /**
     * Get the name of this repository.
     *
     * @return This repository's name.
     */
    String getRepositoryName();
}
