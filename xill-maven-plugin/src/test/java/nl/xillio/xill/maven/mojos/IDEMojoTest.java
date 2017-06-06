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

import me.biesaart.utils.FileUtils;
import nl.xillio.xill.cli.RobotExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Dwight.Peters on 25-Apr-17.
 */
public class IDEMojoTest {

    private IDEMojo mojo;
    private Path dir;

    @BeforeClass
    public void createTempFolders() throws IOException {
        dir = Files.createTempDirectory(getClass().getSimpleName());
    }

    @AfterClass
    private void afterClass() throws IOException {
        FileUtils.deleteDirectory(dir.toFile());
    }

    @BeforeMethod
    public void reset() {
        Artifact dependency1 = mock(Artifact.class);
        Artifact dependency2 = mock(Artifact.class);
        File depFile1 = new File("/absolute/path/to/a/dependency.xlib");
        File depFile2 = new File("/absolute/path/to/another/dependency");
        when(dependency1.getFile()).thenReturn(depFile1);
        when(dependency2.getFile()).thenReturn(depFile2);

        mojo = new IDEMojo(null, dir.toFile(), Arrays.asList(dependency1, dependency2));
        mojo.setClassesDirectory(new File("classes"));
    }

    @Test
    public void testIDE() throws MojoFailureException, MojoExecutionException, RobotExecutionException {
        mojo.execute();
        Assert.assertTrue(Files.exists(dir.resolve(".project")));
    }
}
