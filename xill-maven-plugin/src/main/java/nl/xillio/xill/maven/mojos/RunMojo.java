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

import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.XillParsingException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.maven.services.XillEnvironmentService;
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

    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    private String finalName;

    private boolean hasErrors;

    @Inject
    public RunMojo(XillEnvironmentService environmentService) {
        super(environmentService);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        Path rootFolder = getClassesDirectory();;
        Path[] includePaths = new Path[] {
            rootFolder.resolve(finalName + ".xlib")};

        XillProcessor processor = buildProcessor(mainRobot, rootFolder, includePaths);
        List<Issue> issues = compile(processor);
        issues.forEach(this::logIssue);
        
        processor.getRobot().setArgument(buildArgument());
    }

    private XillProcessor buildProcessor(String robot, Path root, Path[] includes) {
        try {
            return getXillEnvironment().buildProcessor(root, robot, includes);
        } catch (IOException e) {
            getLog().error("Failed to initialize Xill. ", e);
        }
        return null;
    }

    private List<Issue> compile(XillProcessor processor) {
        try {
            return processor.compile();
        } catch (IOException e) {
            getLog().error("Fatal error during compilitation. ", e);
        } catch (XillParsingException e) {
            getLog().error("Compile error: " + e.getMessage(), e);
        }
        return null;
    }

    // TEMP UTIL
    private MetaExpression buildArgument() {
        LinkedHashMap<String, MetaExpression> data = new LinkedHashMap<>(3);

        data.put("input", fromValue(new SimpleIOStream(System.in, "InputStream")));
        data.put("output", fromValue(new SimpleIOStream(System.out, "OutputStream")));
        data.put("error", fromValue(new SimpleIOStream(System.err, "ErrorStream")));

        return fromValue(data);
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
}
