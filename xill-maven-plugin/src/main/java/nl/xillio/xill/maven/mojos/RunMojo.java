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

import n.xillio.xill.cli.RobotExecutionException;
import n.xillio.xill.cli.XillRobotExecutor;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.maven.services.XillEnvironmentService;
import nl.xillio.xill.plugins.system.services.info.SystemInfoService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RunMojo extends AbstractXlibMojo {
    // Configuration.
    @Parameter(defaultValue = "mainRobot", required = true)
    private String mainRobot;

    @Parameter(defaultValue = "${project.artifacts}", readonly = true, required = true)
    private Collection<Artifact> artifacts;

    private boolean hasErrors;

    @Inject
    public RunMojo(XillEnvironmentService environmentService) {
        super(environmentService);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path rootFolder = getClassesDirectory();;
        Path[] includePaths = artifacts.stream().map(Artifact::getFile).map(File::toPath).toArray(Path[]::new);

        XillRobotExecutor robotExecutor = new XillRobotExecutor(
                getXillEnvironment(),
                rootFolder,
                includePaths,
                System.in,
                System.out,
                System.err);

        try {
            robotExecutor.execute(mainRobot);
        } catch (RobotExecutionException e) {
            getLog().error("Execution Error: ", e);
        }
    }
}
