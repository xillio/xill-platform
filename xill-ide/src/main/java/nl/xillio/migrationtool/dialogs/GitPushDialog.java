package nl.xillio.migrationtool.dialogs;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Pair;
import me.biesaart.utils.Log;
import nl.xillio.migrationtool.gui.ProjectPane;
import nl.xillio.xill.versioncontrol.JGitRepository;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Dwight.Peters on 15-Nov-16.
 */
public class GitPushDialog extends FXMLDialog{

    private static final Logger LOGGER = Log.get();

    @FXML
    private TextField message;

    @FXML
    private ListView fileList;

    @FXML
    private Button okBtn;

    private final JGitRepository repo;

    /**
     * Default constructor.
     *
     * @param repo    the JGitRepository that will be pushed to.
     */
    public GitPushDialog(final JGitRepository repo) {
        super("/fxml/dialogs/GitPush.fxml");
        this.setTitle("Push Changes");
        this.repo = repo;

        Set<String> changedFiles = repo.getChangedFiles();
        if (changedFiles != null && changedFiles.size() > 0) {
            fileList.setItems(FXCollections.observableArrayList(changedFiles));
        } else {
            fileList.setItems(FXCollections.observableArrayList("No changes were found"));
            okBtn.setDisable(true);
        }
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void pushBtnPressed(final ActionEvent event) {
        try {
            repo.commit(message.getText());
        }
        catch (GitAPIException e) {
            AlertDialog aDlg = new AlertDialog(Alert.AlertType.ERROR, "Error pushing git", "Pushing has failed", e.getMessage(), ButtonType.OK);
            aDlg.show();
            close();
        }
        try {
            repo.push();
            AlertDialog aDlg = new AlertDialog(Alert.AlertType.INFORMATION, "Succesfull pull", "Pushing has succeeded", "Pushing has succeeded", ButtonType.OK);
            aDlg.show();
            close();
        }
        catch (GitAPIException e) {}
        GitAuthenticateDialog dlg = new GitAuthenticateDialog(repo);
        dlg.showAndWait();
        try{
            repo.push();
            AlertDialog aDlg = new AlertDialog(Alert.AlertType.INFORMATION, "Succesfull push", "PUshing has succeeded", "Pushing has succeeded", ButtonType.OK);
            aDlg.show();
        }
        catch (GitAPIException e) {
            AlertDialog aDlg = new AlertDialog(Alert.AlertType.ERROR, "Error pushing git", "Pushing has failed", e.getMessage(), ButtonType.OK);
            aDlg.show();
        }
        close();
    }
}

