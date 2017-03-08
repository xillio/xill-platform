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
package nl.xillio.xill.plugins.template.services;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.InvalidUserInputException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is responsible for creating a default configuration
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class ConfigurationFactory {
    /**
     * Build the default configuration with the project root as templates directory
     *
     * @param context The construct's context with the robot information attached
     * @return The default configuration with the project root as templates directory
     */
    public Configuration buildDefaultConfiguration(ConstructContext context) {
        Path path = context.getRootRobot().getProjectPath().toPath();
        return buildDefaultConfiguration(path);
    }

    /**
     * Build the default configuration with the given path as templates directory
     *
     * @param templateDirectory The path that contains all the templates
     * @return The default configuration with the defined templates directory
     */
    public Configuration buildDefaultConfiguration(Path templateDirectory) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);

        try {
            cfg.setDirectoryForTemplateLoading(templateDirectory.toFile());
        } catch (IOException e) {
            throw new InvalidUserInputException(
                    e.getMessage(),
                    templateDirectory.toString(),
                    "A valid path to the directory that contains the templates",
                    Paths.get("/templates").toAbsolutePath().toString(),
                    e);
        }

        return cfg;
    }
}
