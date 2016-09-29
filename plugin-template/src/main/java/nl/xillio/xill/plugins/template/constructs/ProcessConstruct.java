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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.template.data.EngineMetadata;
import nl.xillio.xill.plugins.template.services.ConfigurationFactory;
import nl.xillio.xill.plugins.template.services.TemplateProcessor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This construct generates a new file using a template and a data model
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class ProcessConstruct extends Construct {

    private final TemplateProcessor templateProcessor;
    private final ConfigurationFactory configurationFactory;

    @Inject
    public ProcessConstruct(TemplateProcessor templateProcessor, ConfigurationFactory configurationFactory) {
        this.templateProcessor = templateProcessor;
        this.configurationFactory = configurationFactory;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (templateName, output, model, engine) -> process(templateName, output, model, engine, context),
                new Argument("templateName", ATOMIC),
                new Argument("output", ATOMIC),
                new Argument("model", emptyObject(), OBJECT),
                new Argument("engine", NULL, ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression templateName, MetaExpression output, MetaExpression model, MetaExpression engine, ConstructContext context) {
        Configuration configuration = getConfiguration(engine, context);
        OutputStream stream = getOutputStream(output);

        templateProcessor.generate(
                templateName.getStringValue(),
                stream,
                extractValue(model),
                configuration);

        return NULL;
    }

    private Configuration getConfiguration(MetaExpression engine, ConstructContext context) {
        if (engine.isNull()) {
            return configurationFactory.buildDefaultConfiguration(context);
        } else {
            return engine.getMeta(EngineMetadata.class).getConfiguration();
        }
    }

    private OutputStream getOutputStream(MetaExpression output) {
        try {
            return output.getBinaryValue().getOutputStream();
        } catch (IOException e) {
            throw new OperationFailedException(
                    "create a stream.",
                    e.getMessage(),
                    "Did you provide the template engine to this construct?",
                    e
            );
        }
    }
}
