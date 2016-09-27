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
package nl.xillio.xill.constructs;

import com.google.inject.Inject;
import freemarker.template.Configuration;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.data.EngineMetadata;
import nl.xillio.xill.services.ConfigurationFactory;
import nl.xillio.xill.services.files.FileResolver;

import java.nio.file.Path;
import java.util.Map;

/**
 * This construct generates a new file using a template and a data model
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class GetEngineConstruct extends Construct {
    private final ConfigurationFactory configurationFactory;
    private final FileResolver fileResolver;

    @Inject
    public GetEngineConstruct(ConfigurationFactory configurationFactory, FileResolver fileResolver) {
        this.configurationFactory = configurationFactory;
        this.fileResolver = fileResolver;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (options) -> process(options, context),
                new Argument("options", NULL, OBJECT)
        );
    }

    private MetaExpression process(MetaExpression options, ConstructContext context) {
        MetaExpression templateDir = getTemplateDir(options, context);
        Path path = fileResolver.buildPath(context, templateDir);
        Configuration cfg = configurationFactory.parseConfiguration(path, options.getValue());

        MetaExpression result = fromValue("[TemplateEngine: " + path + "]");
        EngineMetadata configuration = new EngineMetadata(cfg);
        result.storeMeta(configuration);
        return result;
    }

    private MetaExpression getTemplateDir(MetaExpression options, ConstructContext context) {
        if (!options.isNull()) {
            Map<String, MetaExpression> optionsObject = options.getValue();

            if (optionsObject.containsKey("templatesDirectory")) {
                return optionsObject.get("templatesDirectory");
            }
        }
        return fromValue(context.getRootRobot().getProjectPath().getPath());
    }
}
