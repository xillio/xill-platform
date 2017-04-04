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

import nl.xillio.xill.api.Issue;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.maven.services.XillEnvironmentService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class CheckMojoTest {
    private XillEnvironment environment;
    private XillProcessor processor;
    private Path robot = Paths.get("robot.xill");

    private File classesDirectory = new File("classes");
    private Path dependencyPath;

    private CheckMojo mojo;

    @BeforeMethod
    public void reset() throws IOException {
        XillEnvironmentService environmentService = mock(XillEnvironmentService.class);
        environment = mock(XillEnvironment.class);
        processor = mock(XillProcessor.class);

        // Mock the environment service.
        when(environmentService.getXillEnvironment()).thenReturn(environment);
        when(environment.buildProcessor(any(), (RobotID) any(), any())).thenReturn(processor);

        // Mock the dependency artifact and file.
        Artifact dependency = mock(Artifact.class);
        dependencyPath = mock(Path.class);
        File depFile = mock(File.class);
        when(dependency.getFile()).thenReturn(depFile);
        when(depFile.toPath()).thenReturn(dependencyPath);

        mojo = new TestableCheckMojo(environmentService, classesDirectory, Collections.singleton(dependency), Collections.singletonList(robot));
    }

    @Test
    public void testCheck() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.execute();

        verify(environment, times(1)).buildProcessor(eq(classesDirectory.toPath()), (RobotID) any(), eq(dependencyPath));
        verify(processor, times(1)).validate();
    }

    @Test
    public void testInfoWarning() throws MojoFailureException, MojoExecutionException {
        List<Issue> issues = new ArrayList<>();
        issues.add(new Issue("Test issue info", -1, Issue.Type.INFO, RobotID.dummyRobot()));
        issues.add(new Issue("Test issue warning", 5, Issue.Type.WARNING, RobotID.dummyRobot()));
        when(processor.validate()).thenReturn(issues);

        mojo.execute();
    }

    @Test(expectedExceptions = MojoFailureException.class)
    public void testError() throws MojoFailureException, MojoExecutionException {
        List<Issue> issues = new ArrayList<>();
        issues.add(new Issue("Test issue error", 1, Issue.Type.ERROR, RobotID.dummyRobot()));
        when(processor.validate()).thenReturn(issues);

        mojo.execute();
    }

    @Test(expectedExceptions = MojoFailureException.class)
    public void testIOException() throws MojoFailureException, MojoExecutionException, IOException {
        when(environment.buildProcessor(any(), (RobotID) any(), any())).thenThrow(new IOException());

        mojo.execute();
    }

    // This class is only used for testing, all it does is override the getRobotFiles() method.
    private class TestableCheckMojo extends CheckMojo {
        private final List<Path> robotFiles;

        private TestableCheckMojo(XillEnvironmentService environmentService, File classesDirectory, Collection<Artifact> artifacts, List<Path> robotFiles) {
            super(environmentService, classesDirectory, artifacts);
            this.robotFiles = robotFiles;
        }

        @Override
        protected List<Path> getRobotFiles() {
            return robotFiles;
        }
    }
}
