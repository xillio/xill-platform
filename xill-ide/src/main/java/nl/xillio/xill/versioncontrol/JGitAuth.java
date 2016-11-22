package nl.xillio.xill.versioncontrol;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.xillio.migrationtool.dialogs.AlertDialog;
import nl.xillio.migrationtool.dialogs.GitAuthenticateDialog;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Class responsible for authentication behavior.
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

    public void setCredentialsProvider(TransportCommand command) {
        if (credentials != null) {
            command.setCredentialsProvider(credentials);
        }
    }

    public boolean getAuthentication() {
        GitAuthenticateDialog dlg = new GitAuthenticateDialog(this);
        dlg.showAndWait();

        return !dlg.isCanceled();
    }

    /*
    This method should probably be in the abstract frontend ..
     */
    private void showError(String action, Throwable cause) {
        new AlertDialog(Alert.AlertType.ERROR, "Error while " + action, "An error occurred while " + action + ".", cause.getMessage()).showAndWait();
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
