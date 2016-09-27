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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * This class is responsible for processing a template given a model and a configuration
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
public class TemplateProcessor {

    /**
     * Combine a template and a model with the given configuration and write the result to the outputstream
     *
     * @param templateName  The name of the template that should be used
     * @param output        The outputstream to which the result should be written
     * @param model         The model that will be combined with the template
     * @param cfg           The configuration of the template engine
     */
    public void generate(String templateName, OutputStream output, Object model, Configuration cfg) {
        OutputStreamWriter writer = new OutputStreamWriter(output);

        try {
            Template template = cfg.getTemplate(templateName);
            template.process(model, writer);

            // Flush the buffer of the writer and do not close it since the underlying stream would be closed as well
            writer.flush();
        } catch (UnsupportedEncodingException e) {
            throw new InvalidUserInputException(
                    "The given encoding is not supported by FreeMarker.",
                    e.getMessage(),
                    "A FreeMarker supported encoding.",
                    "\"encoding\" : \"UNICODE\"",
                    e);
        } catch (IOException | TemplateException e) {
            throw new OperationFailedException("process template.", e.getMessage(), e);
        }
    }
}
