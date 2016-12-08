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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.xill.versioncontrol.GitException;
import nl.xillio.xill.versioncontrol.JGitAuth;
import nl.xillio.xill.versioncontrol.JGitRepository;

/**
 * A generic task class which executes Git operations. Override the {@link #execute} method with actual Git functionality.
 *
 * @author Edward
 */
public abstract class GitOperation extends Task<Void> {
    protected JGitRepository repo;
    private JGitAuth auth;

    /**
     * Create a new Git operation.
     *
     * @param repo The repo that this operation should work on.
     */
    public GitOperation(JGitRepository repo) {
        this.repo = repo;
        this.auth = repo.getAuth();
    }

    protected abstract void execute() throws GitException;

    @Override
    public Void call() {
        tryCommand();
        return null;
    }

    /**
     * Try to run the command.
     * This method will first try to run the command without any authentication.
     * If that fails, the user is prompted for credentials.
     * This will continue until the operation is either successful or there is an error unrelated to authentication.
     */
    private void tryCommand() {
        try {
            execute();
        } catch (GitException e) {
            // Check if the exception is an authorization exception.
            if (auth.isAuthorizationException(e)) {
                boolean reRun = false;

                // Check whether the invalid credentials dialog should be shown.
                if (auth.getCredentials() != null) {
                    auth.openCredentialsInvalidDialog();
                }

                // Check if we should rerun, count down the latches.
                if (auth.getAuthentication()) {
                    reRun = true;
                } else {
                    // If the dialog was canceled, cancel the operation.
                    cancelOperation();
                }

                // Await the credentials latch, check for rerun.
                if (reRun) {
                    tryCommand();
                }
            } else {
                // If there is a different error, handle it.
                cancelOperation();
                handleError(e);
            }
        }
    }

    protected void handleError(Throwable cause) {
        Platform.runLater(() -> new AlertDialog(Alert.AlertType.ERROR, "Error", "An error occurred.", cause.getMessage()).showAndWait());
    }

    /**
     * Cancel the operation and revert any mid-way effects of it.
     */
    protected void cancelOperation() {
    }

    /**
     * Get a thread with this task.
     *
     * @return A thread containing this task.
     */
    public Thread getThread() {
        return new Thread(this, "Git operation thread");
    }
}