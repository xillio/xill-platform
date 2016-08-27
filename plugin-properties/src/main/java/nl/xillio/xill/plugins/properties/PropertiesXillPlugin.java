/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.properties;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.plugins.properties.services.ContextPropertiesResolver;

import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This class represents the Properties plugin for xill.
 * <p>
 * This plugin allows users to define properties for their projects in a hierarchical way.
 *
 * @author Thomas Biesaart
 */
public class PropertiesXillPlugin extends XillPlugin {

    @Provides
    @Named("defaults")
    Properties defaultProperties(XillEnvironment xillEnvironment) {
        Properties defaults = new Properties();
        System.getProperties().forEach(
                (key, value) -> defaults.put("sys." + key, value)
        );
        System.getenv().forEach(
                (key, value) -> {
                    String lowerUnderScore = key.toLowerCase();
                    String name = lowerUnderScore.replaceAll("_", ".");
                    defaults.put("env." + name, value);
                }
        );
        defaults.put(
                "xill.plugins",
                xillEnvironment.getPlugins()
                        .stream()
                        .map(XillPlugin::getName)
                        .collect(Collectors.joining(";"))
        );
        return defaults;
    }

    @Provides
    @Singleton
    ContextPropertiesResolver contextPropertiesResolver() {
        return ContextPropertiesResolver.defaultXillResolver();
    }
}
