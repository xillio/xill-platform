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
package nl.xillio.xill.plugins.document.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.document.services.ContentTypeService;

import static nl.xillio.xill.plugins.document.DocumentXillPlugin.DEFAULT_IDENTITY;

/**
 * This construct is used to define a decorator.
 *
 * @author Thomas Biesaart
 */
public class SaveDecoratorConstruct extends AbstractUDMConstruct {
    private final ContentTypeService contentTypeService;

    @Inject
    public SaveDecoratorConstruct(ContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (name, definition, identity) -> process(name, definition, identity, context),
                new Argument("decoratorName", ATOMIC),
                new Argument("definition", OBJECT),
                new Argument("identity", fromValue(DEFAULT_IDENTITY), ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression name, MetaExpression definition, MetaExpression identity, ConstructContext context) {
        if (name.isNull() || name.getStringValue().trim().isEmpty()) {
            throw new RobotRuntimeException("A decorator must have a non-empty name.");
        }
        performSafe(() ->
                contentTypeService.saveDecorator(context, identity.getStringValue(), name.getStringValue(), definition)
        );
        return fromValue(name.getStringValue());
    }

    @Override
    public boolean hideDocumentation() {
        // See DecoratorConstruct
        return true;
    }
}
