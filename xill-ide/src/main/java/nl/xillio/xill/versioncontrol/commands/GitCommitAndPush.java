package nl.xillio.xill.versioncontrol.commands;

import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Created by Edward on 22/11/2016.
 */
public class GitCommitAndPush extends GitCommandTask {
    private String commitMessage;

    public GitCommitAndPush(JGitRepository repo, String commitMessage) {
        super(repo);
        this.commitMessage = commitMessage;
    }
    public void execute() throws GitAPIException {
        repo.commitCommand(commitMessage);
        repo.pushCommand();
    }
}
