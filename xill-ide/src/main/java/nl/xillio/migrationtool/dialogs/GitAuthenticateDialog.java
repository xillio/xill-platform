package nl.xillio.migrationtool.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

/**
 * Created by Dwight.Peters on 15-Nov-16.
 */
public class GitAuthenticateDialog extends FXMLDialog{

    private static final Logger LOGGER = Log.get();

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label message;

    private final JGitRepository repo;

    public boolean cancled;

    /**
     * Default constructor.
     *
     * @param repo    the JGitRepository that will be pushed to.
     */
    public GitAuthenticateDialog(final JGitRepository repo) {
        super("/fxml/dialogs/GitAuthenticate.fxml");
        this.setTitle("Fill in Credentials");
        this.repo = repo;
        this.cancled = false;
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        cancled = true;
        close();
    }

    @FXML
    private void okBtnPressed(final ActionEvent event) {
        repo.setCredentials(username.getText(), password.getText());
        password.clear();
        close();
    }

    public void badCredentials() {
        message.setText("There may be something wrong with the given username and password.");
    }
}

