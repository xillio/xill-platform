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
package nl.xillio.xill.services;

import com.google.inject.Inject;
import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.services.files.FileResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class is responsible for creating a configuration
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class ConfigurationFactory {
    private static final String TEMPLATES_DIRECTORY = "templatesDirectory";
    private static final String ENCODING = "encoding";
    private static final String NO_CACHING = "noCaching";
    private static final String STRONG_CACHE = "strongCache";
    private static final String SOFT_CACHE = "softCache";
    private final FileResolver fileResolver;

    @Inject
    public ConfigurationFactory(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    /**
     * Parse the configuration by the options given and return the template engines' configuration
     *
     * @param options The options that should be parsed for the configuration
     * @param context The construct's context with the robot information attached
     * @return The configuration for the template engine
     */
    public Configuration parseConfiguration(Map<String, MetaExpression> options, ConstructContext context) {
        if (options == null) {
            return buildDefaultConfiguration(context);
        }

        Configuration cfg = instanceConfiguration(options, context);

        if (options.containsKey(ENCODING)) {
            cfg.setDefaultEncoding(options.get(ENCODING).getStringValue());
        }

        if (options.containsKey(NO_CACHING) && options.get(NO_CACHING).getBooleanValue()) {
            cfg.setCacheStorage(new NullCacheStorage());
        } else {
            String strongCache = get(options, STRONG_CACHE).orElse("0");
            String softCache = get(options, SOFT_CACHE).orElse(Integer.toString(Integer.MAX_VALUE));
            setSetting(
                    cfg,
                    Configuration.CACHE_STORAGE_KEY,
                    "strong:" + strongCache + ", soft:" + softCache,
                    () -> SOFT_CACHE + ": " + softCache + ", " + STRONG_CACHE + ": " + strongCache
            );
        }

        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return cfg;
    }

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

    private Configuration buildDefaultConfiguration(Path templatePath) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);

        try {
            cfg.setDirectoryForTemplateLoading(templatePath.toFile());
        } catch (IOException e) {
            throw new InvalidUserInputException(
                    e.getMessage(),
                    templatePath.toString(),
                    "A valid path to the directory that contains the templates",
                    Paths.get("/templates").toAbsolutePath().toString(),
                    e);
        }

        return cfg;
    }

    private Configuration instanceConfiguration(Map<String, MetaExpression> options, ConstructContext context) {
        if (options.containsKey(TEMPLATES_DIRECTORY)) {
            return buildDefaultConfiguration(
                    fileResolver.buildPath(context, options.get(TEMPLATES_DIRECTORY))
            );
        } else {
            return buildDefaultConfiguration(context);
        }
    }

    private void setSetting(Configuration cfg, String cacheStorageKey, String value, Supplier<String> input) {
        try {
            cfg.setSetting(cacheStorageKey, value);
        } catch (TemplateException e) {
            throw new InvalidUserInputException(
                    e.getMessage(),
                    input.get(),
                    "A valid integer",
                    "{'strongCache': 50}",
                    e
            );
        }
    }

    private Optional<String> get(Map<String, MetaExpression> data, String key) {
        return Optional.ofNullable(data.get(key)).map(MetaExpression::getStringValue);
    }
}
