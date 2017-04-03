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
package nl.xillio.xill.mojos;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

import java.io.File;
import java.io.IOException;

@Mojo(name = "xlib", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class XlibMojo extends AbstractXlibMojo {
    private static final String TYPE = "xlib";

    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    private String finalName;

    // Configuration.
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    @Component(role = Archiver.class, hint = "zip")
    private Archiver archiver;

    public void execute() throws MojoExecutionException {
        File archive = createArchive();

        // Check if the project already has an artifact.
        File artifactFile = artifact.getFile();
        if (artifactFile != null && artifactFile.isFile()) {
            throw new MojoExecutionException("An artifact is already set for this project.");
        }

        artifact.setFile(archive);
    }

    private File createArchive() throws MojoExecutionException {
        File archive = new File(outputDirectory, finalName + '.' + TYPE);

        // Set the destination file and add the files to the archive.
        archiver.setDestFile(archive);
        archiver.addFileSet(new DefaultFileSet(getClassesDirectory().toFile()));

        // Try to create the archive.
        try {
            archiver.createArchive();
            return archive;
        } catch (IOException e) {
            throw new MojoExecutionException("Error assembling " + TYPE, e);
        }
    }
}
