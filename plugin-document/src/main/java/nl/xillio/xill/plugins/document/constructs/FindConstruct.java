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

import com.mongodb.client.FindIterable;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import org.bson.Document;

import static nl.xillio.xill.plugins.document.DocumentXillPlugin.DEFAULT_IDENTITY;

/**
 * This construct will find all documents that match a filter.
 *
 * @author Thomas Biesaart
 */
public class FindConstruct extends AbstractUDMConstruct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (filter, sort, identity) -> process(filter, sort, identity, context),
                new Argument("filter", emptyObject(), OBJECT),
                new Argument("sort", emptyObject(), OBJECT),
                new Argument("identity", fromValue(DEFAULT_IDENTITY), ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression filter, MetaExpression sort, MetaExpression identity, ConstructContext context) {
        FindIterable<Document> result = performSafe(
                () -> getDocuments(context, identity.getStringValue())
                        .find(toDocument(filter))
                        .sort(toDocument(sort))
                        .noCursorTimeout(true)
        );

        return fromValue(result);
    }

    private MetaExpression fromValue(FindIterable<Document> result) {
        MetaExpression expression = fromValue("Document.find()");
        expression.storeMeta(
                new MetaExpressionIterator<>(
                        result.iterator(),
                        this::fromValue
                )
        );
        return expression;
    }
}
