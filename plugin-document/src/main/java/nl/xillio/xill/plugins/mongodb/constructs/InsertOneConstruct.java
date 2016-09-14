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

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.bson.Document;


/**
 * This construct represents the insertOne method on MongoDB.
 *
 * @author Edward van Egdom
 */
public class InsertOneConstruct extends AbstractCollectionApiConstruct {
    @Override
    @SuppressWarnings("squid:S2095")
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("document", ExpressionDataType.OBJECT),
        };
    }

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        Document insert = toDocument(arguments[0]);

        tryInsertOne(collection, insert);
        return NULL;
    }

    private void tryInsertOne(MongoCollection<Document> collection, Document document) {
        try {
            collection.insertOne(document);
        } catch (MongoException e) {
            throw new RobotRuntimeException("Insert failed: " + e.getMessage(), e);
        }
    }
}
