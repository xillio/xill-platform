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
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.xill.versioncontrol.JGitAuth;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * A generic task class which executes Git operations. Override the {@link #execute} method with actual Git functionality.
 *
 * @author Edward
 */
public abstract class GitOperation extends Task<Void> {
    private static final Logger LOGGER = Log.get();

    protected JGitRepository repo;

    private JGitAuth auth;

    // Latch on this tasks state, should countdown on finish
    private CountDownLatch commandLatch;

    // Keep track of need for re-run of command
    private boolean reRun;

    /**
     * Create a new Git operation.
     *
     * @param repo The repo that this operation should work on.
     */
    public GitOperation(JGitRepository repo) {
        this.commandLatch = new CountDownLatch(1);
        this.repo = repo;
        this.auth = repo.getAuth();
    }

    protected abstract void execute() throws GitAPIException;

    @Override
    public Void call() {
        tryCommand();
        awaitLatch(commandLatch);
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
            commandLatch.countDown();
        } catch (GitAPIException e) {
            // Check if the exception is an authorization exception.
            if (auth.isAuthorizationException(e)) {
                final CountDownLatch credentialsLatch = new CountDownLatch(1); // Latch on the credentials dialog
                Platform.runLater(() -> {
                    reRun = false;

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
                        commandLatch.countDown();
                    }
                    credentialsLatch.countDown();
                });

                // Await the credentials latch, check for rerun.
                if (awaitLatch(credentialsLatch) && reRun) {
                    tryCommand();
                }
            } else {
                // If there is a different error, handle it.
                cancelOperation();
                handleError(e);
                commandLatch.countDown();
            }
        }
    }

    protected void handleError(Throwable cause) {
        try {
            Platform.runLater(() -> new AlertDialog(Alert.AlertType.ERROR, "Error", "An error occurred.", cause.getMessage()).showAndWait());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private boolean awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
            return true;
        } catch (InterruptedException e) {
            LOGGER.error("Caught interrupted exception while waiting for Git operation to complete.");
        }
        return false;
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