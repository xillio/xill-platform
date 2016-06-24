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
import com.mongodb.client.model.IndexOptions;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.services.IndexOptionsFactory;
import org.bson.Document;

/**
 * This construct represents the createIndex method on MongoDB.
 *
 * @author Thomas Biesaart
 * @see <a href="https://docs.mongodb.org/v3.0/reference/method/db.collection.createIndex/#db.collection.createIndex">db.collection.createIndex</a>
 */
public class CreateIndexConstruct extends AbstractCollectionApiConstruct {
    private final IndexOptionsFactory indexOptionsFactory;

    @Inject
    public CreateIndexConstruct(IndexOptionsFactory indexOptionsFactory) {
        this.indexOptionsFactory = indexOptionsFactory;
    }


    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("keys", OBJECT),
                new Argument("options", emptyObject(), OBJECT)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        Document keys = toDocument(arguments[0]);

        IndexOptions options = indexOptionsFactory.build(arguments[1]);
        String result = collection.createIndex(keys, options);
        return fromValue(result);
    }
}
