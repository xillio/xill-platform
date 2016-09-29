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
package nl.xillio.xill.plugins.template.constructs;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.template.data.EngineMetadata;
import nl.xillio.xill.plugins.template.services.ConfigurationFactory;
import nl.xillio.xill.plugins.template.services.ConfigurationParser;
import nl.xillio.xill.services.files.FileResolver;

import java.util.HashMap;

/**
 * This construct generates a new file using a template and a data model
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class GetEngineConstruct extends Construct {
    private static final String TEMPLATES_DIRECTORY = "templatesDirectory";

    private final ConfigurationFactory configurationFactory;
    private final ConfigurationParser configurationParser;
    private final FileResolver fileResolver;

    @Inject
    public GetEngineConstruct(ConfigurationFactory configurationFactory, ConfigurationParser configurationParser, FileResolver fileResolver) {
        this.configurationFactory = configurationFactory;
        this.configurationParser = configurationParser;
        this.fileResolver = fileResolver;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (options) -> process(options, context),
                new Argument("options", emptyObject(), OBJECT)
        );
    }

    private MetaExpression process(MetaExpression options, ConstructContext context) {
        HashMap<String, MetaExpression> optionsObject = options.getValue();
        Configuration defaultConfiguration = getConfiguration(optionsObject, context);
        Configuration cfg = configurationParser.parseConfiguration(defaultConfiguration, optionsObject);

        MetaExpression result = fromValue("[TemplateEngine]");
        EngineMetadata configuration = new EngineMetadata(cfg);
        result.storeMeta(configuration);
        return result;
    }

    private Configuration getConfiguration(HashMap<String, MetaExpression> options, ConstructContext context) {
        if (options.isEmpty() || !options.containsKey(TEMPLATES_DIRECTORY)) {
            return configurationFactory.buildDefaultConfiguration(context);
        } else {
            return configurationFactory.buildDefaultConfiguration(
                    fileResolver.buildPath(context, options.get(TEMPLATES_DIRECTORY))
            );
        }
    }
}
