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
import com.mongodb.client.model.FindOneAndDeleteOptions;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.services.FindOneAndDeleteOptionsFactory;
import org.bson.Document;

/**
 * This construct represents the <code>FindOneAndDelete()</code> method on MongoDB. That method is specific to the Java
 * API of MongoDB and therefore not documented in the online javascript API documentation. It replaces the
 * <code>FindAndModify()</code> method.
 *
 * <code>FindOneAndDelete()</code> in MongoDB deletes the first document returned by <code>query</code>.
 *
 * @author Titus Nachbauer
 */
public class FindOneAndDeleteConstruct extends AbstractCollectionApiConstruct {
    private final FindOneAndDeleteOptionsFactory findOneAndDeleteOptionsFactory;

    @Inject
    public FindOneAndDeleteConstruct(FindOneAndDeleteOptionsFactory findOneAndDeleteOptionsFactory) {
        this.findOneAndDeleteOptionsFactory = findOneAndDeleteOptionsFactory;
    }

    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("query", emptyObject(), OBJECT),
                new Argument("options", emptyObject(), OBJECT)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        Document filter = toDocument(arguments[0]);

        FindOneAndDeleteOptions options = findOneAndDeleteOptionsFactory.build(arguments[1]);

        Document result = collection.findOneAndDelete(filter, options);
        return toExpression(result);
    }
}
