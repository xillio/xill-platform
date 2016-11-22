package nl.xillio.xill.versioncontrol.commands;

import nl.xillio.xill.versioncontrol.JGitRepository;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Git pull task
 * @author Edward van Egdom
 */
public class GitPull extends GitCommandTask {
    public GitPull(JGitRepository repo) {
        super(repo);
    }
    public void execute() throws GitAPIException {
        repo.pullCommand();
    }
}
