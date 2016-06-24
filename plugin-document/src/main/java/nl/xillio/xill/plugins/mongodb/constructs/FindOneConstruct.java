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

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import org.bson.Document;

/**
 * This construct represents the findOne method on MongoDB using the findConstruct.
 * It extends the findConstruct since the arguments and the services are identical.
 *
 * @author Thomas Biesaart
 * @author Pieter Dirk Soels
 * @see FindConstruct
 * @see <a href="https://docs.mongodb.org/v3.0/reference/method/db.collection.findOne/#db.collection.findOne">db.collection.findOne</a>
 */
public class FindOneConstruct extends FindConstruct {

    @Override
    MetaExpression process(MetaExpression[] arguments, MongoCollection<Document> collection, ConstructContext context) {
        FindIterable<Document> mongoResult = findIterableBuilder.getIterable(collection, arguments);
        return toExpression(mongoResult.first());
    }
}
