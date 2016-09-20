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
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.TemplateService;
import nl.xillio.xill.services.files.FileResolver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * This construct generates a new file using a template and a data model
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class GenerateConstruct extends Construct {

    private final TemplateService templateService;
    private final FileResolver fileResolver;

    @Inject
    public GenerateConstruct(TemplateService templateService, FileResolver fileResolver) {
        this.templateService = templateService;
        this.fileResolver = fileResolver;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (templatePath, output, model, configuration)
                        -> process(templatePath, output, model, configuration, context),
                new Argument("templatePath", ATOMIC),
                new Argument("output", ATOMIC),
                new Argument("model", OBJECT),
                new Argument("configuration", NULL, ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression templatePath, MetaExpression output, MetaExpression model, MetaExpression configuration, ConstructContext context) {
        Path path = fileResolver.buildPath(context, templatePath);
        OutputStream stream;

        try {
            stream = output.getBinaryValue().getOutputStream();
        } catch (IOException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }

        templateService.generate(
                path,
                stream,
                extractValue(model),
                configuration.getStringValue());

        return NULL;
    }
}
