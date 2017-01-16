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

import nl.xillio.xill.versioncontrol.GitException;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class GitCommitAndPushOperationTest {
    private JGitRepository repo;
    private GitCommitAndPushOperation operation;
    private String commitMessage = "Commit message here";

    @BeforeMethod
    private void reset() {
        repo = mock(JGitRepository.class);
        operation = new GitCommitAndPushOperation(repo, commitMessage);
    }

    @Test
    public void testNormal() throws GitException {
        operation.execute();

        verify(repo, times(1)).commitCommand(commitMessage);
        verify(repo, times(1)).pushCommand();
    }

    @Test
    public void testAlreadyCommitted() throws GitException{
        GitCommitAndPushOperation gitOperation = new GitCommitAndPushOperation(repo, commitMessage);
        gitOperation.execute();
        gitOperation.execute();

        verify(repo, times(1)).commitCommand(any());
        verify(repo, times(2)).pushCommand();
    }

    @Test
    public void testCancel() throws GitException {
        operation.cancelOperation();
        verify(repo, times(1)).resetCommitCommand();
    }
}
