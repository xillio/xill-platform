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

import javafx.application.Platform;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.dialogs.GitAuthenticateDialog;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Class responsible for Git credentials management.
 *
 * @author Edward van Egdom
 */
public class JGitAuth {
    private static final Logger LOGGER = Log.get();
    private static final Map<File, JGitAuth> AUTH_POOL = new HashMap<>();

    private CredentialsProvider credentials;

    private JGitAuth() {
    }

    /**
     * Get a JGitAuth object from the auth pool, based on the git folder location.
     *
     * @param gitFolder The git folder location.
     * @return The JGitAuth object.
     */
    public static JGitAuth get(File gitFolder) {
        // If the pool contains an entry, use that.
        if (AUTH_POOL.containsKey(gitFolder))
            return AUTH_POOL.get(gitFolder);

        // Add a new entry to the pool.
        JGitAuth newAuth = new JGitAuth();
        AUTH_POOL.put(gitFolder, newAuth);
        return newAuth;
    }

    /**
     * Set the credentials.
     *
     * @param username The username.
     * @param password The password.
     */
    public void setCredentials(String username, String password) {
        credentials = new UsernamePasswordCredentialsProvider(username, password);
    }

    /**
     * Get the credentials.
     *
     * @return The credentials provider.
     */
    public CredentialsProvider getCredentials() {
        return credentials;
    }

    /**
     * Show the authentication dialog.
     *
     * @return true if the credentials were entered, or false if the dialog was canceled.
     */
    public boolean getAuthentication(boolean invalidCredentials) {
        final FutureTask<Boolean> dialog = new FutureTask<>(() -> {
            GitAuthenticateDialog dlg = new GitAuthenticateDialog(this);
            dlg.setIncorrectLoginLabelToVisible(invalidCredentials);
            dlg.showAndWait();
            return dlg.isCanceled();
        });
        Platform.runLater(dialog);

        boolean authDialogCanceled;
        try {
            authDialogCanceled = dialog.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while authenticating", e);
            authDialogCanceled = true;
        }

        return !authDialogCanceled;
    }

    /**
     * Check whether an exception is an authorization exception.
     *
     * @param e The exception to check.
     * @return Whether the exception is an authorization exception.
     */
    public boolean isAuthorizationException(GitException e) {
        return e.getMessage().contains(JGitText.get().notAuthorized) || e.getMessage().contains(JGitText.get().noCredentialsProvider);
    }

}
