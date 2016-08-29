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
package nl.xillio.xill.plugins.properties.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.plugins.properties.services.PropertyService;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;

/**
 * This construct will resolve a property using the properties service.
 *
 * @author Thomas Biesaart
 */
class GetConstruct extends Construct {
    private final PropertyService propertyService;

    @Inject
    public GetConstruct(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (name, defaultValue) -> process(name, defaultValue, context),
                new Argument("name", ATOMIC),
                new Argument("defaultValue", NULL)
        );
    }

    private MetaExpression process(MetaExpression name, MetaExpression defaultValue, ConstructContext context) {
        assertNotNull(name, "property name");

        String path = name.getStringValue();

        context.addRobotStoppedListener(e -> propertyService.clean());

        String result = propertyService
                .getFormattedProperty(path, defaultValue.isNull() ? null : defaultValue.getStringValue(), context);
        if(result == null) {
            return NULL;
        }

        return fromValue(result.replaceAll("\\$\\$", "\\$"));
    }
}
