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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class JGitRepositoryTest {
    private static final Logger LOGGER = Log.get();

    private Git git;
    private JGitAuth auth;
    private JGitRepository repository;

    private JGitRepository createJGitRepository(Git repo, JGitAuth jGitAuth) {
        JGitRepository result = new JGitRepository(null);

        // Set the fields using reflection.
        Class<?> c = result.getClass();
        try {
            Field repoField = c.getDeclaredField("repository");
            repoField.setAccessible(true);
            repoField.set(result, repo);

            Field authField = c.getDeclaredField("auth");
            authField.setAccessible(true);
            authField.set(result, jGitAuth);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Error while setting JGitRepository fields for testing.", e);
        }

        return result;
    }

    @BeforeMethod
    private void reset() {
        git = mock(Git.class);
        auth = mock(JGitAuth.class);
        repository = createJGitRepository(git, auth);
    }

    @Test
    public void isInitializedTest() {
        assertTrue(repository.isInitialized());

        JGitRepository noInit = createJGitRepository(null, null);
        assertFalse(noInit.isInitialized());
    }

    @Test
    public void getRepositoryNameTest() {
        String name = "repo name";

        // Mock.
        Repository repo = mock(Repository.class);
        File dir = mock(File.class);
        File parent = mock(File.class);
        when(git.getRepository()).thenReturn(repo);
        when(repo.getDirectory()).thenReturn(dir);
        when(dir.getParentFile()).thenReturn(parent);
        when(parent.getName()).thenReturn(name);

        assertEquals(repository.getRepositoryName(), name);
    }
}
