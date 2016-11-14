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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of {@link Repository} which uses the jGit library for interacting with Git repositories.
 * @author Daan Knoope
 */
public class JGitRepository implements Repository {
    private static Git repository;


    public JGitRepository(File path){
        File ceilingDirectory = path.getParentFile(); // check this
        FileRepositoryBuilder builder = new FileRepositoryBuilder()
                .addCeilingDirectory(ceilingDirectory)
                .findGitDir(path);
        repository = null;
        try{
            repository = new Git(builder.build());
        } catch (IOException e) {
            // Do something usefull here
        } catch(IllegalArgumentException e){
            // Could not find repo in this directory
        }
    }

    @Override
    public boolean Commit(String commitMessage) {
        try {
            repository.add().addFilepattern("--all").call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
        try {
            repository.commit().setMessage(commitMessage).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    @Override
    public boolean Push() {
        try {
            repository.push().call();
        } catch (GitAPIException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean Pull() {
        try {
            repository.pull().call();
        } catch (GitAPIException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isInitialized() {
        return repository != null;
    }
}
