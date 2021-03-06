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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.xillio.xill.api.ProjectFile;
import nl.xillio.xill.maven.services.XillEnvironmentService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;

@Mojo(name = "ide", defaultPhase = LifecyclePhase.INSTALL, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class IDEMojo extends AbstractXlibMojo {
    private static final String PROJECT_FILE_NAME = ".project";
    @Parameter(defaultValue = "src/main/resources", required = true)
    private File projectDirectory;
    @Parameter(defaultValue = "${project.artifacts}", readonly = true, required = true)
    private Collection<Artifact> artifacts;

    @Inject
    public IDEMojo(XillEnvironmentService environmentService) {
        super(environmentService);
    }

    public IDEMojo(XillEnvironmentService environmentService, File projectDirectory, Collection<Artifact> artifacts) {
        this(environmentService);
        this.projectDirectory = projectDirectory;
        this.artifacts = artifacts;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try (OutputStream stream = Files.newOutputStream(projectDirectory.toPath().resolve(PROJECT_FILE_NAME), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ProjectFile projectFile = new ProjectFile();
            projectFile.setRobotPath(
                    Arrays.stream(getRobotPaths(artifacts))
                            .skip(1)
                            .toArray(Path[]::new)
            );
            stream.write(gson.toJson(projectFile).getBytes());
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
