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

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import org.bson.BsonValue;
import org.bson.Document;

/**
 * This construct represents the distinct method on MongoDB.
 *
 * @author Thomas Biesaart
 * @see <a href="https://docs.mongodb.org/v3.0/reference/method/db.collection.distinct/#db.collection.distinct">db.collection.distinct</a>
 */
public class DistinctConstruct extends AbstractCollectionApiConstruct {

    @Override
    @SuppressWarnings("squid:S2095")  // Suppress "Resources should be closed": Arguments do not need to be closed here, because ConstructProcessor closes them
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("field", ATOMIC),
                new Argument("query", emptyObject(), OBJECT)
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        String field = arguments[0].getStringValue();
        Document query = toDocument(arguments[1]);

        DistinctIterable<BsonValue> mongoResult = collection.distinct(field, BsonValue.class).filter(query);

        return fromValueRaw(mongoResult, collection, arguments);
    }
}
