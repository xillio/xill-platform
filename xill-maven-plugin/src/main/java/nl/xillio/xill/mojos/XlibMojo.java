/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.mojos;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

import java.io.File;
import java.io.IOException;

@Mojo(name = "xlib", defaultPhase = LifecyclePhase.PACKAGE)
public class XlibMojo extends AbstractMojo {
    private static final String TYPE = "xlib";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    private String finalName;
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true, required = true)
    private File classesDirectory;

    // Configuration.
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    @Component(role = Archiver.class, hint = "zip")
    private Archiver archiver;

    public void execute() throws MojoExecutionException {
        File archive = createArchive();

        // Check if the project already has an artifact.
        if (projectHasArtifact()) {
            throw new MojoExecutionException("An artifact is already set for this project.");
        }

        project.getArtifact().setFile(archive);
    }

    private File createArchive() throws MojoExecutionException {
        File archive = new File(outputDirectory, finalName + '.' + TYPE);

        archiver.setDestFile(archive);
        archiver.addFileSet(new DefaultFileSet(classesDirectory));

        try {
            archiver.createArchive();
            return archive;
        } catch (IOException e) {
            throw new MojoExecutionException("Error assembling " + TYPE, e);
        }
    }

    private boolean projectHasArtifact() {
        File file = project.getArtifact().getFile();
        return file != null && file.isFile();
    }
}
