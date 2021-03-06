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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import org.bson.Document;

/**
 * This construct represents the getIndexes method on MongoDB (implemented in the Java API as <code>listIndexes()</code>)
 *
 * @author Titus Nachbauer
 * @see <a href="https://docs.mongodb.org/v3.0/reference/method/db.collection.getindexes/#db.collection.getIndexes">db.collection.getIndexes</a>
 */
public class ListIndexesConstruct extends AbstractCollectionApiConstruct {

    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {

        MongoIterable<Document> result = collection.listIndexes();

        return fromValue(result, collection);
    }
}
