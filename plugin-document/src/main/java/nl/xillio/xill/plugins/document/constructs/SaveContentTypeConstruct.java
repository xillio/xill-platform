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
import nl.xillio.xill.plugins.document.services.ContentTypeService;

import java.util.List;
import java.util.stream.Collectors;

import static nl.xillio.xill.plugins.document.DocumentXillPlugin.DEFAULT_IDENTITY;

/**
 * This construct is used to save a content type to the database.
 *
 * @author Thomas Biesaart
 */
public class SaveContentTypeConstruct extends AbstractUDMConstruct {
    private final ContentTypeService contentTypeService;

    @Inject
    public SaveContentTypeConstruct(ContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (contentTypeName, decorators, identity) -> process(contentTypeName, decorators, identity, context),
                new Argument("contentTypeName", ATOMIC),
                new Argument("decorators", LIST),
                new Argument("identity", fromValue(DEFAULT_IDENTITY), ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression contentTypeName, MetaExpression decorators, MetaExpression identity, ConstructContext context) {
        List<String> decoratorNames = decorators.<List<MetaExpression>>getValue()
                .stream()
                .map(MetaExpression::getStringValue)
                .collect(Collectors.toList());

        performSafe(() ->
                contentTypeService.saveContentType(context, identity.getStringValue(), contentTypeName.getStringValue(), decoratorNames)
        );
        return fromValue(contentTypeName.getStringValue());
    }

    @Override
    public boolean hideDocumentation() {
        return true;
    }
}
