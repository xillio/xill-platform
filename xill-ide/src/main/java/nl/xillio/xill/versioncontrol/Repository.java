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

import java.util.Set;

/**
 * Representation of a version control repository.
 *
 * @author Daan Knoope
 */
public interface Repository {
    /**
     * Commits all changes with the provi;ded commit message.
     *
     * @param commitMessage The message for the commit.
     * @return {@code true} if commit succeeded, {@code false} otherwise.
     */
    //void commit(String commitMessage);

    /**
     * Pushes all changes to the remote repository.
     */
    void push();

    /**
     * Pulls all changes from the remote repository.
     */
    void pull();

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
     * Set the credentials for the repository.
     *
     * @param username The username.
     * @param password The password.
     */
    void setCredentials(String username, String password);
}
