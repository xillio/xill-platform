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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class JGitRepositoryTest {
    private static final Logger LOGGER = Log.get();

    private Git git;
    private JGitAuth auth;
    private JGitRepository repository;

    private JGitRepository createJGitRepository(Git repo, JGitAuth jGitAuth) {
        JGitRepository result = new JGitRepository(null);

        // Simple mock for the credentials provider.
        when(auth.getCredentials()).thenReturn(mock(CredentialsProvider.class));

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
    public void testIsInitialized() {
        assertTrue(repository.isInitialized());

        JGitRepository noInit = createJGitRepository(null, null);
        assertFalse(noInit.isInitialized());
    }

    @Test
    public void testGetRepositoryName() {
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

    @Test
    public void testPushCommand() throws GitException {
        // Mock.
        PushCommand cmd = mock(PushCommand.class);
        when(git.push()).thenReturn(cmd);
        when(cmd.setCredentialsProvider(auth.getCredentials())).thenReturn(cmd);

        // Run.
        repository.pushCommand();

        // Verify.
        verify(cmd, times(1)).setCredentialsProvider(auth.getCredentials());
        try {
            verify(cmd, times(1)).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e.getCause());
        }
    }

    @Test
    public void testPullCommand() throws GitException {
        // Mock.
        PullCommand cmd = mock(PullCommand.class);
        when(git.pull()).thenReturn(cmd);
        when(cmd.setCredentialsProvider(auth.getCredentials())).thenReturn(cmd);

        // Run.
        repository.pullCommand();

        // Verify.
        verify(cmd, times(1)).setCredentialsProvider(auth.getCredentials());
        try {
            verify(cmd, times(1)).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e.getCause());
        }
    }

    @Test
    public void testCommitCommand() throws GitException {
        String message = "commit message";

        // Mock.
        AddCommand add = mock(AddCommand.class);
        when(git.add()).thenReturn(add);
        when(add.addFilepattern(".")).thenReturn(add);
        when(add.setUpdate(true)).thenReturn(add);
        CommitCommand commit = mock(CommitCommand.class);
        when(git.commit()).thenReturn(commit);
        when(commit.setMessage(message)).thenReturn(commit);

        // Run.
        repository.commitCommand(message);

        // Verify.
        verify(add, times(2)).addFilepattern(".");
        verify(add, times(1)).setUpdate(true);
        try {
            verify(add, times(2)).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e.getCause());
        }
        verify(commit, times(1)).setMessage(message);
        try {
            verify(commit, times(1)).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e.getCause());
        }
    }

    @Test
    public void testRevertCommitCommand() throws GitException {
        // Mock.
        ResetCommand cmd = mock(ResetCommand.class);
        when(git.reset()).thenReturn(cmd);
        when(cmd.setMode(ResetCommand.ResetType.MIXED)).thenReturn(cmd);
        when(cmd.setRef("HEAD^")).thenReturn(cmd);

        // Run.
        repository.resetCommitCommand();

        // Verify.
        verify(cmd, times(1)).setMode(ResetCommand.ResetType.MIXED);
        verify(cmd, times(1)).setRef("HEAD^");
        try {
            verify(cmd, times(1)).call();
        } catch (GitAPIException e) {
            throw new GitException(e.getMessage(), e.getCause());
        }
    }

    @Test
    public void testGetChangedFiles() throws GitAPIException {
        Set<String> uncommitted = new HashSet<>();
        uncommitted.add("not committed");
        Set<String> untracked = new HashSet<>();
        untracked.add("not tracked");
        Set<String> total = new HashSet<>(uncommitted);
        total.addAll(untracked);

        // Mock.
        StatusCommand cmd = mock(StatusCommand.class);
        when(git.status()).thenReturn(cmd);
        Status status = mock(Status.class);
        when(cmd.call()).thenReturn(status);
        when(status.getUncommittedChanges()).thenReturn(uncommitted);
        when(status.getUntracked()).thenReturn(untracked);

        // Run.
        Set<String> result = repository.getChangedFiles();

        // Assert.
        assertEquals(result, total);
    }

    @Test
    public void testGetChangedFilesException() throws GitAPIException {
        // Mock.
        StatusCommand cmd = mock(StatusCommand.class);
        when(git.status()).thenReturn(cmd);
        when(cmd.call()).thenThrow(new GitAPIException("This exception is supposed to be thrown.") {
        });

        // Run.
        Set<String> result = repository.getChangedFiles();

        // Assert.
        assertEquals(result, Collections.emptySet());
    }
}
