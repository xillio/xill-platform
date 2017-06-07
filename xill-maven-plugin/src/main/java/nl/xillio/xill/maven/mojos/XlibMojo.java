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
import nl.xillio.xill.maven.services.XillEnvironmentService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.archiver.Archiver;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "xlib", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class XlibMojo extends AbstractXlibMojo {
    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;
    @Parameter(defaultValue = "${project.artifacts}", readonly = true, required = true)
    private Collection<Artifact> artifacts;

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

    @Inject
    public XlibMojo(XillEnvironmentService environmentService, FileSetFactory fileSetFactory) {
        super(environmentService);
        this.fileSetFactory = fileSetFactory;
    }

    public XlibMojo(XillEnvironmentService environmentService, FileSetFactory fileSetFactory,
                    Artifact artifact, String finalName, File outputDirectory, Archiver archiver) {
        this(environmentService, fileSetFactory);
        this.artifact = artifact;
        this.finalName = finalName;
        this.outputDirectory = outputDirectory;
        this.archiver = archiver;
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

        // Try to create the archive.
        try {
            archiver.createArchive();
            return archive;
        } catch (IOException e) {
            throw new MojoExecutionException("Error assembling xlib.", e);
        }
    }

    private void collectFatArchiveRobots() throws MojoExecutionException {
        List<Artifact> artifactList = new ArrayList<>(artifacts);

        // Get all resources from this project, these should not be overwritten.
        Set<String> resourceSet = getFilesFromProject();

        // Reverse for-loop to retain the correct overriding robot precedence.
        for (int i = artifactList.size() - 1; i >= 0; i--) {
            Artifact artifact = artifactList.get(i);

            // Skip non-Xill dependencies.
            if (!"xlib".equals(artifact.getType())) {
                getLog().warn("Skipping non-xlib artifact: " + artifact.getId());
                continue;
            }

            // Get the file system for the xlib.
            Path artifactPath = artifact.getFile().toPath();
            extractArchive(artifactPath, resourceSet);
        }
    }

    private void extractArchive(Path archive, Set<String> resourceSet) throws MojoExecutionException {
        URI artifactUri = URI.create("jar:" + archive.toUri());

        // Get the file system for the archive.
        FileSystem fs;
        try {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            fs = FileSystems.newFileSystem(artifactUri, env);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not read from xlib: " + archive, e);
        }

        // Copy all files.
        Path classesDir = getClassesDirectory();
        try {
            Files.walkFileTree(fs.getPath(FileSetFactory.ROBOTS_DIRECTORY), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Get the file name without the "robots/" prefix, copy the file.
                    String fileName = file.toString().substring(FileSetFactory.ROBOTS_DIRECTORY.length() + 1);

                    // If the file already existed in this project, don't overwrite it.
                    if (!resourceSet.contains(fileName)) {
                        Path target = classesDir.resolve(fileName);
                        Files.createDirectories(target.getParent());
                        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Could not copy file from artifact: " + archive, e);
        }
    }

    private Set<String> getFilesFromProject() throws MojoExecutionException {
        int start = getClassesDirectory().toString().length() + 1;
        try {
            return Files.walk(getClassesDirectory())
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .map(s -> s.substring(start))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new MojoExecutionException("Could not get list of files.");
        }
    }
}
