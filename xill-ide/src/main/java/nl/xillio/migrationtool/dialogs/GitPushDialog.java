package nl.xillio.migrationtool.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

/**
 * Created by Dwight.Peters on 15-Nov-16.
 */
public class GitPushDialog extends FXMLDialog{

    @FXML
    public TextField message;

    private final JGitRepository repo;

    private static final Logger LOGGER = Log.get();

    /**
     * Default constructor.
     *
     * @param repo    the JGitRepository that will be pushed to.
     */
    public GitPushDialog(final JGitRepository repo) {
        super("/fxml/dialogs/GitPush.fxml");
        this.setTitle("Push Changes");
        this.repo = repo;
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void pushBtnPressed(final ActionEvent event) {
        try {
            repo.commit(message.getText());
            repo.push();
            close();
        }
        catch (GitAPIException e) {}
        GitAuthenticateDialog dlg = new GitAuthenticateDialog(repo);
        dlg.showAndWait();
        try{
            repo.commit(message.getText());
            repo.push();
        }
        catch (GitAPIException e) {
            AlertDialog aDlg = new AlertDialog(Alert.AlertType.ERROR, "Error pushing git", "Pushing has failed", e.getMessage(), ButtonType.OK);
            aDlg.show();
        }
        close();
    }
}

