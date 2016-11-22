package nl.xillio.xill.versioncontrol.commands;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.GitAuthenticateDialog;
import nl.xillio.xill.versioncontrol.JGitAuth;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.JGitText;

import java.util.concurrent.CountDownLatch;

/**
 * A generic task class which executes Git commands. Override the {@link #execute} method with actual Git functionality.
 *
 * @Author Edward
 */

abstract class GitCommandTask extends Task<Void> {
    JGitRepository repo;

    JGitAuth auth;

    // Latch on this tasks state, should countdown on finish
    CountDownLatch commandLatch;

    // Keep track of need for re-run of command
    boolean reRun;

    public GitCommandTask(JGitRepository repo) {
        this.commandLatch = new CountDownLatch(1);
        this.repo = repo;
        this.auth = repo.getAuth();
    }

    abstract void execute() throws GitAPIException;

    @Override
    public Void call() {
        tryCommand();

        try {
            commandLatch.await();
        } catch (InterruptedException e) {
            Platform.exit();
        }

        return null;
    }

    private void tryCommand() {
        try {
            execute();
            commandLatch.countDown();
        } catch (GitAPIException e) {
            if (e instanceof TransportException && auth.isAuthorizationException(e)) {
                final CountDownLatch credentialsLatch = new CountDownLatch(1); // Latch on the credentials dialog
                Platform.runLater(() -> {
                    reRun = false;
                    if (auth.getCredentials() != null)
                        auth.openCredentialsDialog();

                    if (auth.getAuthentication()) {
                        reRun = true;
                    } else {
                        commandLatch.countDown();
                    }
                    credentialsLatch.countDown();
                });
                try {
                    credentialsLatch.await();

                    if (reRun) {
                        tryCommand();
                    }
                } catch (InterruptedException ie) {
                    Platform.exit();
                }
            } else {
                showError(e); // Error handling
                commandLatch.countDown();
            }
        }
    }

    private void showError(Throwable cause) {
        Platform.runLater(() -> new AlertDialog(Alert.AlertType.ERROR, "Error", "An error occurred.", cause.getMessage()).showAndWait());
    }
}