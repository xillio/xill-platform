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
package nl.xillio.xill.plugins.system.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.system.services.properties.SystemPropertiesService;

/**
 * Gets a list or a single system property
 */
public class PropertiesConstruct extends Construct {

    @Inject
    SystemPropertiesService propertiesService;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                prop -> process(prop, propertiesService),
                new Argument("property", NULL, ATOMIC));
    }

    static MetaExpression process(final MetaExpression property, final SystemPropertiesService properties) {

        if (property.isNull()) {
            return parseObject(properties.getProperties());
        }

        return fromValue(properties.getProperty(property.getStringValue()));
    }
}
