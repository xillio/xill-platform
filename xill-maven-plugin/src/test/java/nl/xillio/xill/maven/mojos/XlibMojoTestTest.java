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
import nl.xillio.xill.maven.services.FileSystemFactory;
import nl.xillio.xill.maven.services.FilesService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.FileSet;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

public class XlibMojoTestTest {
    private final String finalName = "final-name";
    private final File outputDirectory = new File("target");

    @Test
    public void testCreateArchive() throws MojoExecutionException, IOException {
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        FileSet fileSet = mock(FileSet.class);
        FileSystem fileSystem = mock(FileSystem.class);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);
        when(fileSystemFactory.createFileSystem(any(), any())).thenReturn(fileSystem);

        XlibMojo mojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, null, finalName, outputDirectory, archiver, new File("/"), false);
        mojo.setClassesDirectory(new File("classes"));

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
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        FileSet fileSet = mock(FileSet.class);
        FileSystem fileSystem = mock(FileSystem.class);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);
        when(fileSystemFactory.createFileSystem(any(), any())).thenReturn(fileSystem);

        XlibMojo mojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, null, finalName, outputDirectory, archiver, new File("/"), false);
        mojo.setClassesDirectory(new File("classes"));

        doThrow(new IOException()).when(archiver).createArchive();

        mojo.execute();
    }

    @Test(expectedExceptions = MojoExecutionException.class)
    public void testCreateArchiveArchiverException() throws Exception {
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        FileSet fileSet = mock(FileSet.class);
        FileSystem fileSystem = mock(FileSystem.class);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);
        when(fileSystemFactory.createFileSystem(any(), any())).thenReturn(fileSystem);

        XlibMojo mojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, null, finalName, outputDirectory, archiver, new File("/"), false);
        mojo.setClassesDirectory(new File("classes"));

        doThrow(ArchiverException.class).when(archiver).createArchive();

        mojo.execute();

    }

    @Test(expectedExceptions = MojoExecutionException.class)
    public void testArtifactHasFile() throws Exception {
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        FileSet fileSet = mock(FileSet.class);
        FileSystem fileSystem = mock(FileSystem.class);

        File artifactFile = mock(File.class);
        when(artifact.getFile()).thenReturn(artifactFile);
        when(artifactFile.isFile()).thenReturn(true);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);
        when(fileSystemFactory.createFileSystem(any(), any())).thenReturn(fileSystem);

        doThrow(ArchiverException.class).when(archiver).createArchive();
        XlibMojo mojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, null, finalName, outputDirectory, archiver, new File("/"), false);
        mojo.setClassesDirectory(new File("classes"));

        mojo.execute();
    }

    @Test
    public void testCreateFATArchive() throws MojoExecutionException, IOException {
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        FileSet fileSet = mock(FileSet.class);
        FileSystem fileSystem = mock(FileSystem.class);
        Collection artifacts = Arrays.asList(artifact, artifact);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);
        when(fileSystemFactory.createFileSystem(any(), any())).thenReturn(fileSystem);

        XlibMojo includerMojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, artifacts, finalName, outputDirectory, archiver, new File("/"), true);
        includerMojo.setClassesDirectory(new File("classes"));

        File archive = new File(outputDirectory, finalName + ".xlib");

        when(artifact.getFile()).thenReturn(new File("testing"));
        when(artifact.getType()).thenReturn("xlib");
        when(fileSystem.getPath(any())).thenReturn(Paths.get("testing"));

        includerMojo.execute();

        verify(archiver, times(1)).setDestFile(archive);
        verify(archiver, times(2)).addFileSet(fileSet);
        verify(archiver, times(1)).createArchive();
        verify(artifact, times(1)).setFile(archive);
    }

    @Test
    public void testFileVisit() throws Exception {
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        Collection artifacts = Arrays.asList(artifact, artifact);

        XlibMojo includerMojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, artifacts, finalName, outputDirectory, archiver, new File("/"), true);
        includerMojo.setClassesDirectory(new File("classes"));

        Path path = Paths.get("robots", "testing");
        includerMojo.visitFilePath(path);

        verify(filesService, times(1)).copy(path, Paths.get("/testing"), StandardCopyOption.REPLACE_EXISTING);

    }

    @Test
    public void testCreateFATArchiveNoXlib() throws MojoExecutionException, IOException {
        Artifact artifact = mock(Artifact.class);
        Archiver archiver = mock(Archiver.class);
        FileSetFactory fileSetFactory = mock(FileSetFactory.class);
        FileSystemFactory fileSystemFactory = mock(FileSystemFactory.class);
        FilesService filesService = mock(FilesService.class);
        FileSet fileSet = mock(FileSet.class);
        FileSystem fileSystem = mock(FileSystem.class);
        Collection artifacts = Arrays.asList(artifact, artifact);

        when(fileSetFactory.createFileSet(any())).thenReturn(fileSet);
        when(fileSystemFactory.createFileSystem(any(), any())).thenReturn(fileSystem);

        XlibMojo includerMojo = new XlibMojo(null, fileSetFactory, fileSystemFactory, filesService, artifact, artifacts, finalName, outputDirectory, archiver, new File("/"), true);
        includerMojo.setClassesDirectory(new File("classes"));

        File archive = new File(outputDirectory, finalName + ".xlib");

        when(artifact.getFile()).thenReturn(new File("testing"));
        when(artifact.getType()).thenReturn("noXlib");
        when(fileSystem.getPath(any())).thenReturn(Paths.get("testing"));

        includerMojo.execute();

        verify(archiver, times(1)).setDestFile(archive);
        verify(archiver, times(2)).addFileSet(fileSet);
        verify(archiver, times(1)).createArchive();
        verify(artifact, times(1)).setFile(archive);
    }
}
