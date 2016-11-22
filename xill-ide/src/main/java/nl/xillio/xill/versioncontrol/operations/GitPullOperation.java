package nl.xillio.xill.versioncontrol.operations;

import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Git pull task
 * @author Edward van Egdom
 */
public class GitPullOperation extends GitOperation {
    public GitPullOperation(JGitRepository repo) {
        super(repo);
    }
    public void execute() throws GitAPIException {
        repo.pullCommand();
    }
}
