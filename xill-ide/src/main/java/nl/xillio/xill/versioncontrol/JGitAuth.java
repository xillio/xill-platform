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

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.GitAuthenticateDialog;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Class responsible for credentials management.
 * @author Edward van Egdom
 */
public class JGitAuth {
    private CredentialsProvider credentials;

    public JGitAuth() {
    }

    public void setCredentials(String username, String password) {
        credentials = new UsernamePasswordCredentialsProvider(username, password);
    }

    public CredentialsProvider getCredentials() {
        return credentials;
    }

    public boolean getAuthentication() {
        GitAuthenticateDialog dlg = new GitAuthenticateDialog(this);
        dlg.showAndWait();

        return !dlg.isCanceled();
    }

    public boolean isAuthorizationException(GitAPIException e) {
        return e.getMessage().contains(JGitText.get().notAuthorized) || e.getMessage().contains(JGitText.get().noCredentialsProvider);
    }

    public void openCredentialsDialog() {
        new CredentialsAlertDialog().showAndWait();
    }

    class CredentialsAlertDialog extends AlertDialog {
        public CredentialsAlertDialog() {
            super(Alert.AlertType.WARNING, "Invalid credentials", "",
                    "The credentials you entered are invalid, please try again.", ButtonType.OK);
        }
    }
}
