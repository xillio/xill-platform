package nl.xillio.xill.mojos;

import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.services.XillEnvironmentService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractXlibMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File classesDirectory;

    @Inject
    private XillEnvironmentService environmentService;

    protected Path getClassesDirectory() {
        return classesDirectory.toPath();
    }

    protected XillEnvironment getXillEnvironment() {
        return environmentService.getXillEnvironment();
    }

    protected RobotID getRobotId(Path robot) throws MojoExecutionException {
        URL robotUrl;
        try {
            robotUrl = robot.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Could not create URL for robot: " + robot, e);
        }

        String relative = getClassesDirectory().relativize(robot).toString();
        return new RobotID(robotUrl, relative);
    }
}
