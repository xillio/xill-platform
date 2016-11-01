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
package nl.xillio.xill.docgen;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class represents the configuration of the DocGen system
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public class DocGenConfiguration {
    private static final String TEMPLATE_ROOT = "templatesUrl";
    private static final String RESOURCE_ROOT = "resourcesUrl";
    private static final String CONFIG_URL = "/docgen.properties";
    private static final Logger LOGGER = Log.get();
    private final Properties properties;

    public DocGenConfiguration() {
        properties = new Properties(getDefaults());
        try {
            InputStream stream = getClass().getResourceAsStream(CONFIG_URL);
            if (stream == null) {
                throw new FileNotFoundException("Properties file " + CONFIG_URL + " does not exist");
            }
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.warn("Could not load " + CONFIG_URL + " using defaults...", e);
        }
    }

    /**
     * Load all the defaults for this configuration
     *
     * @return the defaults
     */
    private Properties getDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty(TEMPLATE_ROOT, "templates");
        defaults.setProperty(RESOURCE_ROOT, "static");
        return defaults;
    }

    public String getResourceUrl() {
        return properties.getProperty(RESOURCE_ROOT);
    }

    /**
     * Get the url to the template package
     *
     * @return the url
     */
    public String getTemplateUrl() {
        return properties.getProperty(TEMPLATE_ROOT);
    }
}
