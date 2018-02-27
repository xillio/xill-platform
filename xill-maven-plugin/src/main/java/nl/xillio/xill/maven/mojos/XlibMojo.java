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
import nl.xillio.xill.maven.services.XillEnvironmentService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Mojo(name = "xlib", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class XlibMojo extends AbstractXlibMojo {
    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;
    @Parameter(defaultValue = "${project.artifacts}", readonly = true, required = true)
    private Collection<Artifact> artifacts;
    @Parameter(defaultValue = "${project.build.directory}/robots", readonly = true, required = true)
    private File robotsFolder;

    /**
     * The output directory to create the xlib archive in.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Whether to create a fat archive containing all dependencies from this project.
     */
    @Parameter(defaultValue = "false", required = true, property = "includeDependencies")
    private boolean includeDependencies;

    @Component(role = Archiver.class, hint = "zip")
    private Archiver archiver;

    private FileSetFactory fileSetFactory;
    private FileSystemFactory fileSystemFactory;
    private FilesService filesService;

    @Inject
    public XlibMojo(XillEnvironmentService environmentService, FileSetFactory fileSetFactory, FileSystemFactory fileSystemFactory, FilesService filesService) {
        super(environmentService);
        this.fileSetFactory = fileSetFactory;
        this.fileSystemFactory = fileSystemFactory;
        this.filesService = filesService;
    }

    public XlibMojo(XillEnvironmentService environmentService, FileSetFactory fileSetFactory, FileSystemFactory fileSystemFactory, FilesService filesService,
                    Artifact artifact, Collection<Artifact> artifacts, String finalName, File outputDirectory, Archiver archiver, File robotsFolder, boolean includeDependencies) {
        this(environmentService, fileSetFactory, fileSystemFactory, filesService);
        this.artifact = artifact;
        this.artifacts = artifacts;
        this.finalName = finalName;
        this.outputDirectory = outputDirectory;
        this.archiver = archiver;
        this.robotsFolder = robotsFolder;
        this.includeDependencies = includeDependencies;
    }

    public void execute() throws MojoExecutionException {
        // Check if the project already has an artifact.
        File artifactFile = artifact.getFile();
        if (artifactFile != null && artifactFile.isFile()) {
            throw new MojoExecutionException("An artifact is already set for this project.");
        }

        if (includeDependencies) {
            collectFatArchiveRobots();
        }

        artifact.setFile(createArchive());
    }

    private File createArchive() throws MojoExecutionException {
        File archive = new File(outputDirectory, finalName + ".xlib");

        // Set the destination file and add the files to the archive.
        archiver.setDestFile(archive);
        archiver.addFileSet(fileSetFactory.createFileSet(getClassesDirectory()));
        if (robotsFolder.exists()) {
            archiver.addFileSet(fileSetFactory.createFileSet(robotsFolder.toPath()));
        }

        // Try to create the archive.
        try {
            // Tell the archiver to leave the first file,
            archiver.setDuplicateBehavior(Archiver.DUPLICATES_PRESERVE);

            archiver.createArchive();
            return archive;
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Error copying files. ", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error assembling xlib.", e);
        }
    }

    private void collectFatArchiveRobots() throws MojoExecutionException {
        List<Artifact> artifactList = new ArrayList<>(artifacts);

        // Reverse for-loop to retain the correct overriding robot precedence.
        Collections.reverse(artifactList);
        for (Artifact artifact : artifactList) {
            // Skip non-Xill dependencies.
            if (!"xlib".equals(artifact.getType())) {
                getLog().warn("Skipping non-xlib artifact: " + artifact.getId());
                continue;
            }

            // Get the file system for the xlib.
            Path artifactPath = artifact.getFile().toPath();
            copyFromDependency(artifactPath);

        }
    }

    private void copyFromDependency(Path archive) throws MojoExecutionException {
        try (FileSystem fileSystem = openFileSystem(archive.toUri())) {

            filesService.walkFileTree(fileSystem.getPath(FileSetFactory.ROBOTS_DIRECTORY), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    return visitFilePath(path);
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Could not copy file from artifact: " + archive, e);
        }
    }

    private FileSystem openFileSystem(URI archive) throws MojoExecutionException {
        try {
            URI artifactUri = new URI("jar:" + archive.getScheme(), archive.getPath(), null);
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            return fileSystemFactory.createFileSystem(artifactUri, env);
        } catch (IOException | URISyntaxException e) {
            throw new MojoExecutionException("Could not read from xlib: " + archive, e);
        }
    }

    FileVisitResult visitFilePath(Path path) throws IOException {
        // Get the file name without the "robots/" prefix, copy the file.
        Path targetFolder = path.resolve("/" + FileSetFactory.ROBOTS_DIRECTORY);
        Path target = robotsFolder.toPath().resolve(targetFolder.relativize(path).toString());
        filesService.createDirectories(target.getParent());
        filesService.copy(path, target, StandardCopyOption.REPLACE_EXISTING);

        return FileVisitResult.CONTINUE;
    }
}
