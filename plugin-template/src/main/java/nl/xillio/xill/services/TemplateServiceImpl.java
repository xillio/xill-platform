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
import freemarker.template.TemplateExceptionHandler;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

/**
 * This class is the main implementation of the {@link TemplateService}
 *
 * @author Pieter Soels
 * @since 3.5.0
 */
class TemplateServiceImpl implements TemplateService {

    @Override
    public String generate(Path input, OutputStream output, Object model, String configuration) {
        OutputStreamWriter writer = new OutputStreamWriter(output);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);

        try {
            cfg.setDirectoryForTemplateLoading(input.toFile().getParentFile());
            Template template = cfg.getTemplate(input.toFile().getName());
            template.process(model, writer);
        } catch (IOException | TemplateException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
        return "";
    }
}
