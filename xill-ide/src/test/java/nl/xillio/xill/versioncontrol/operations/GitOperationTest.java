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
package nl.xillio.xill.versioncontrol.operations;

import javafx.embed.swing.JFXPanel;
import nl.xillio.xill.versioncontrol.GitException;
import nl.xillio.xill.versioncontrol.JGitAuth;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class GitOperationTest {
    private static final GitException GIT_EXCEPTION = new GitException("This exception is supposed to be thrown.") {
    };

    private JGitRepository repo;
    private JGitAuth auth;
    private GitOperation operation;

    @BeforeMethod
    private void reset() {
        repo = mock(JGitRepository.class);
        auth = mock(JGitAuth.class);
        when(repo.getAuth()).thenReturn(auth);

        // This initializes the JavaFX runtime, which is needed for Platform.runLater.
        new JFXPanel();
    }

    @Test
    public void testNormal() throws GitException {
        operation = spy(new NopGitOperation(repo));
        verify(repo, times(1)).getAuth();

        operation.call();

        // Verify that (apart from the getAuth) there are no interactions with the repo.
        verifyNoMoreInteractions(repo, auth);
        verify(operation, times(1)).execute();
    }

    @Test
    public void testNonAuthException() throws GitException {
        operation = spy(new ExceptionGitOperation(repo));
        when(auth.isAuthorizationException(GIT_EXCEPTION)).thenReturn(false);

        operation.call();

        // Verify.
        verify(auth, times(1)).isAuthorizationException(GIT_EXCEPTION);
        verify(operation, times(1)).execute();
        verify(operation, times(1)).handleError(GIT_EXCEPTION);
        verify(operation, times(1)).cancelOperation();
    }

    @Test
    public void testAuthException() throws GitException {
        operation = spy(new ExceptionGitOperation(repo));
        when(auth.isAuthorizationException(GIT_EXCEPTION)).thenReturn(true);
        when(auth.getCredentials()).thenReturn(null);
        when(auth.getAuthentication()).thenReturn(true);

        operation.call();

        // Verify.
        verify(auth, times(1)).isAuthorizationException(GIT_EXCEPTION);
        verify(auth, never()).openCredentialsInvalidDialog();
        verify(operation, times(2)).execute();
    }

    @Test
    public void testAuthInvalidCancel() throws GitException {
        operation = spy(new ExceptionGitOperation(repo));
        when(auth.isAuthorizationException(GIT_EXCEPTION)).thenReturn(true);
        when(auth.getCredentials()).thenReturn(mock(CredentialsProvider.class));
        when(auth.getAuthentication()).thenReturn(false);

        operation.call();

        // Verify.
        verify(auth, times(1)).isAuthorizationException(GIT_EXCEPTION);
        verify(auth, times(1)).openCredentialsInvalidDialog();
        verify(operation, times(1)).execute();
        verify(operation, times(1)).cancelOperation();
    }

    /**
     * A no-op Git operation.
     */
    private class NopGitOperation extends GitOperation {
        public NopGitOperation(JGitRepository repo) {
            super(repo);
        }

        @Override
        protected void execute() throws GitException {
        }
    }

    /**
     * A Git operation that throws an exception once.
     */
    private class ExceptionGitOperation extends GitOperation {
        boolean thrown = false;

        public ExceptionGitOperation(JGitRepository repo) {
            super(repo);
        }

        @Override
        protected void execute() throws GitException {
            if (!thrown) {
                thrown = true;
                throw GIT_EXCEPTION;
            }
        }

        @Override
        protected void handleError(Throwable cause) {
        }
    }
}
