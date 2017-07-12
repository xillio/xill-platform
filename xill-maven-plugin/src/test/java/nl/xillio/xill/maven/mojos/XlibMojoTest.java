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

import nl.xillio.xill.maven.services.FileSetFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.FileSet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public class XlibMojoTest {
    private Artifact artifact;
    private String finalName = "final-name";
    private File outputDirectory = new File("target");
    private Archiver archiver;
    private FileSet fileSet;

    private XlibMojo mojo;

    @BeforeMethod
    public void reset() {
        artifact = mock(Artifact.class);
        archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        fileSet = mock(FileSet.class);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);

        mojo = new XlibMojo(null, fileSetFactory, artifact, finalName, outputDirectory, archiver, new File("/"));
        mojo.setClassesDirectory(new File("classes"));
    }

    @Test
    public void testCreateArchive() throws MojoExecutionException, IOException {
        File archive = new File(outputDirectory, finalName + ".xlib");
        when(artifact.getFile()).thenReturn(null);

        mojo.execute();

        verify(archiver, times(1)).setDestFile(archive);
        verify(archiver, times(2)).addFileSet(fileSet);
        verify(archiver, times(1)).createArchive();
        verify(artifact, times(1)).setFile(archive);
    }

    @Test(expectedExceptions = MojoExecutionException.class)
    public void testIOException() throws MojoExecutionException, IOException {
        doThrow(new IOException()).when(archiver).createArchive();

        mojo.execute();
    }

    @Test(expectedExceptions = MojoExecutionException.class)
    public void testArtifactHasFile() throws MojoExecutionException {
        File artifactFile = mock(File.class);
        when(artifact.getFile()).thenReturn(artifactFile);
        when(artifactFile.isFile()).thenReturn(true);

        mojo.execute();
    }
}
