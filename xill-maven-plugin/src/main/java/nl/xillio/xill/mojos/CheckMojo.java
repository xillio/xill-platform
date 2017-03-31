/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.mojos;

import me.biesaart.utils.FileUtils;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.services.XillEnvironmentService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

@Mojo(name = "check", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CheckMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.artifacts}", readonly = true, required = true)
    private Collection<Artifact> artifacts;
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File classesDirectory;

    @Inject
    private XillEnvironmentService environmentService;

    private boolean hasErrors;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path[] robotPaths = artifacts.stream().map(Artifact::getFile).map(File::toPath).toArray(Path[]::new);

        Collection<File> files = FileUtils.listFiles(classesDirectory, null, true);
        for (File file : files) {
            validateRobot(getRobotId(file.toPath()), robotPaths);
        }

        if (hasErrors) {
            throw new MojoFailureException("Errors occurred during robot compilation.");
        }
    }

    private void validateRobot(RobotID robot, Path[] robotPaths) {
        try {
            environmentService.getXillEnvironment().buildProcessor(classesDirectory.toPath(), robot, robotPaths).validate()
                    .forEach(this::logIssue);
        } catch (IOException e) {
            getLog().error("Could not build Xill processor for robot: " + robot.getResourcePath(), e);
            hasErrors = true;
        }
    }

    private void logIssue(Issue issue) {
        String message = String.format("%s:%d - %s", issue.getRobot().getURL(), issue.getLine(), issue.getMessage());

        switch (issue.getSeverity()) {
            case INFO:
                getLog().info(message);
                break;
            case WARNING:
                getLog().warn(message);
                break;
            case ERROR:
                getLog().error(message);
                hasErrors = true;
                break;
        }
    }

    private RobotID getRobotId(Path robot) throws MojoExecutionException {
        URL robotUrl;
        try {
            robotUrl = robot.toUri().toURL();
        } catch (MalformedURLException e) {
            getLog().error("Could not create URL for robot: " + robot, e);
            throw new MojoExecutionException("Could not create URL for robot: " + robot, e);
        }

        String relative = classesDirectory.toPath().relativize(robot).toString();
        return new RobotID(robotUrl, relative);
    }
}
