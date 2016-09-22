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
/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.inject.ImplementedBy;
import freemarker.template.Configuration;

import java.io.OutputStream;
import java.nio.file.Path;

/**
 * This interface represents some of the operations for the {@link TemplateServiceImpl}.
 *
 * @author Pieter Soels
 * @since 3.5.0
 */

@ImplementedBy(TemplateServiceImpl.class)
public interface TemplateService {

    /**
     * Set the default configuration for the use of the template engine
     *
     * @param input The directory of the templates
     * @return the default configuration for the template engine
     */
    Configuration getDefaultConfiguration(Path input);

    /**
     * Set the configuration for the use of the template engine
     *
     * @param input     The directory of the templates
     * @param options   Options to set the configuration to
     * @return the parsed configuration for the template engine
     */
    Configuration parseConfiguration(Path input, Object options);

    /**
     * Generate textual file from given template and model
     *
     * @param templateName  The name and extension of the template file
     * @param output        The writer to write the result to
     * @param model         The data model to insert in the template
     * @param configuration The generated configuration for the template engine
     * @return null
     */
    String generate(String templateName, OutputStream output, Object model, Configuration configuration);
}
