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
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillProcessor;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.maven.services.XillEnvironmentService;
import nl.xillio.xill.maven.services.XillRobotExecutorService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class RunMojoTest {
    private XillRobotExecutor executor;

    private File classesDirectory = new File(".");
    private String mainRobot = "mainRobot";

    private RunMojo mojo;

    @BeforeMethod
    public void reset() throws IOException {
        XillEnvironmentService environmentService = mock(XillEnvironmentService.class);
        XillRobotExecutorService robotExecutorService = mock(XillRobotExecutorService.class);
        XillEnvironment environment = mock(XillEnvironment.class);
        XillProcessor processor = mock(XillProcessor.class);
        executor = mock(XillRobotExecutor.class);

        // Mock the environment and robot executor service.
        when(environmentService.getXillEnvironment()).thenReturn(environment);
        when(environment.buildProcessor(any(), (RobotID) any(), any())).thenReturn(processor);
        when(robotExecutorService.getRobotExecutor(any(), any(), any(), any(), any(), any())).thenReturn(executor);

        // Mock the dependency artifact and file.
        Artifact dependency = mock(Artifact.class);
        Path dependencyPath = mock(Path.class);
        File depFile = mock(File.class);
        when(dependency.getFile()).thenReturn(depFile);
        when(depFile.toPath()).thenReturn(dependencyPath);

        mojo = new RunMojo(environmentService, robotExecutorService, classesDirectory, Collections.singleton(dependency), mainRobot);
    }

    @Test
    public void testCheck() throws MojoFailureException, MojoExecutionException, RobotExecutionException {
        mojo.execute();

        verify(executor, times(1)).execute(mainRobot);
    }

    @Test(expectedExceptions = MojoFailureException.class)
    public void testRobotException() throws MojoFailureException, MojoExecutionException, RobotExecutionException {
        doThrow(new RobotExecutionException("")).when(executor).execute(mainRobot);

        mojo.execute();

        verify(executor, times(1)).execute(mainRobot);
    }
}
