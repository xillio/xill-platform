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
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Created by Dwight.Peters on 15-Nov-16.
 */
public class GitPushDialog extends FXMLDialog{

    @FXML
    private TextField message;

    @FXML
    private ListView fileList;

    private final JGitRepository repo;
    private final ProjectPane projectPane;

    private static final Logger LOGGER = Log.get();

    /**
     * Default constructor.
     *
     * @param projectPane the projectPane to which this dialog is attached to.
     * @param repo    the JGitRepository that will be pushed to.
     */
    public GitPushDialog(final ProjectPane projectPane, final JGitRepository repo) {
        super("/fxml/dialogs/GitPush.fxml");
        this.setTitle("Push Changes");
        this.projectPane = projectPane;
        this.repo = repo;

        Set<String> changedFiles = repo.getChangedFiles();
        if (changedFiles != null && changedFiles.size() > 0) {
            fileList.setItems(FXCollections.observableArrayList(changedFiles));
        } else {
            fileList.setItems(FXCollections.observableArrayList("No changes were found"));
        }
    }

    @FXML
    private void cancelBtnPressed(final ActionEvent event) {
        close();
    }

    @FXML
    private void pushBtnPressed(final ActionEvent event) {
        repo.commit(message.getText());
        repo.push();
        close();
    }
}

