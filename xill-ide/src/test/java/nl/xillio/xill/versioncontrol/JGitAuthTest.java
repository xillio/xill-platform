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

import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

public class JGitAuthTest {
    private JGitAuth auth = JGitAuth.get(new File(""));

    @Test
    public void testCredentialsProvider() {
        String user = "Admin";
        String pass = "P@ssw0rd!";

        // Set the credentials.
        auth.setCredentials(user, pass);
        CredentialsProvider provider = auth.getCredentials();

        // Get the credentials.
        CredentialItem.Username userItem = new CredentialItem.Username();
        provider.get(null, userItem);
        CredentialItem.Password passItem = new CredentialItem.Password();
        provider.get(null, passItem);

        // Assert.
        assertEquals(userItem.getValue(), user);
        assertEquals(String.valueOf(passItem.getValue()), pass);
    }

    @Test
    public void testAuthorizationException() {
        GitException noAuthException = new GitException("something something") {
        };
        GitException authException = new GitException(JGitText.get().notAuthorized) {
        };

        assertFalse(auth.isAuthorizationException(noAuthException));
        assertTrue(auth.isAuthorizationException(authException));
    }

    @Test
    public void testAuthPool() {
        // These should return the same auth object, because they have the same directory.
        JGitAuth one = JGitAuth.get(new File("some/dir"));
        JGitAuth sameAsOne = JGitAuth.get(new File("some/dir"));

        // This is a different directory, and thus should be a different auth object.
        JGitAuth other = JGitAuth.get(new File("other/dir"));

        assertSame(sameAsOne, one);
        assertNotSame(other, one);
    }
}
