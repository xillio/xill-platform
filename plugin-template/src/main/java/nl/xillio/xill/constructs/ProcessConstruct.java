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
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.data.ConfigurationMetadata;
import nl.xillio.xill.services.TemplateService;
import nl.xillio.xill.services.files.FileResolver;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This construct generates a new file using a template and a data model
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class ProcessConstruct extends Construct {

    private final TemplateService templateService;
    private final FileResolver fileResolver;

    @Inject
    public ProcessConstruct(TemplateService templateService, FileResolver fileResolver) {
        this.templateService = templateService;
        this.fileResolver = fileResolver;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (templateName, output, model, configuration)
                        -> process(templateName, output, model, configuration, context),
                new Argument("templateName", ATOMIC),
                new Argument("output", ATOMIC),
                new Argument("model", OBJECT),
                new Argument("configuration", ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression templateName, MetaExpression output, MetaExpression model, MetaExpression configuration, ConstructContext context) {
        Configuration cfg = configuration.getMeta(ConfigurationMetadata.class).getConfiguration();
        OutputStream stream;

        try {
            stream = output.getBinaryValue().getOutputStream();
        } catch (IOException e) {
            throw new OperationFailedException("create a stream.", e.getMessage() , e);
        }

        templateService.generate(
                templateName.getStringValue(),
                stream,
                extractValue(model),
                cfg);

        return NULL;
    }
}
