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

import freemarker.cache.NullCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class is the main implementation of the {@link TemplateService}
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
class TemplateServiceImpl implements TemplateService {
    private final static String ENCODING = "encoding";
    private final static String LOG_TEMPLATE_EXCEPTIONS = "logTemplateExceptions";
    private final static String NO_CACHING = "noCaching";
    private final static String STRONG_CACHE = "strongCache";
    private final static String SOFT_CACHE = "softCache";

    @Override
    public Configuration getDefaultConfiguration(Path templatePath) {
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

    @Override
    public Configuration parseConfiguration(Path templatePath, Map<String, MetaExpression> options) {
        Configuration cfg = getDefaultConfiguration(templatePath);

        if (options == null) {
            return cfg;
        }

        if (options.containsKey(ENCODING)) {
            cfg.setDefaultEncoding(options.get(ENCODING).getStringValue());
        }
        if (options.containsKey(LOG_TEMPLATE_EXCEPTIONS)) {
            cfg.setLogTemplateExceptions(options.get(LOG_TEMPLATE_EXCEPTIONS).getBooleanValue());
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

    @Override
    public void generate(String templateName, OutputStream output, Object model, Configuration cfg) {
        OutputStreamWriter writer = new OutputStreamWriter(output);

        try {
            Template template = cfg.getTemplate(templateName);
            template.process(model, writer);

            // Flush the buffer of the writer and do not close it since the underlying stream would be closed as well
            writer.flush();
        } catch (IOException | TemplateException e) {
            throw new OperationFailedException("processing of template.", e.getMessage(), e);
        }
    }
}
