package nl.xillio.xill.versioncontrol.commands;

import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Created by Edward on 22/11/2016.
 */
public class GitPull extends GitCommandTask {
    public GitPull(JGitRepository repo) {
        super(repo);
    }
    public void execute() throws GitAPIException {
        repo.pullCommand();
    }
}
