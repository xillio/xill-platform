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
package nl.xillio.xill.maven.mojos;

import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.maven.services.XillEnvironmentService;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractXlibMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File classesDirectory;

    private XillEnvironmentService environmentService;

    @Inject
    public AbstractXlibMojo(XillEnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    public AbstractXlibMojo(XillEnvironmentService environmentService, File classesDirectory) {
        this.environmentService = environmentService;
        this.classesDirectory = classesDirectory;
    }

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

    protected List<Path> getRobotFiles() {
        return FileUtils.listFiles(classesDirectory, new String[]{"xill"}, true)
                .stream().map(File::toPath).collect(Collectors.toList());
    }
}
