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
package nl.xillio.xill.plugins.mongodb.constructs;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.services.FindOneAndUpdateOptionsFactory;
import org.bson.Document;

/**
 * This construct represents the <code>FindOneAndUpdate()</code> method on MongoDB. That method is specific to the Java
 * API of MongoDB and therefore not documented in the online javascript API documentation. It replaces the
 * <code>FindAndModify()</code> method.
 *
 * <code>FindOneAndUpdate()</code> in MongoDB updates the first document returned by a <code>query</code> with an <code>update</code> document.
 *
 * @author Titus Nachbauer
 */
public class FindOneAndUpdateConstruct extends AbstractCollectionApiConstruct {
    private final FindOneAndUpdateOptionsFactory findOneAndUpdateOptionsFactory;

    @Inject
    public FindOneAndUpdateConstruct(FindOneAndUpdateOptionsFactory findOneAndUpdateOptionsFactory) {
        this.findOneAndUpdateOptionsFactory = findOneAndUpdateOptionsFactory;
    }

    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("query", emptyObject(), OBJECT),
                new Argument("update", emptyObject(), OBJECT),
                new Argument("options", emptyObject(), OBJECT)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        Document filter = toDocument(arguments[0]);
        Document update = toDocument(arguments[1]);
        FindOneAndUpdateOptions options = findOneAndUpdateOptionsFactory.build(arguments[2]);

        Document result = collection.findOneAndUpdate(filter, update, options);
        return toExpression(result);
    }
}
