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
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.bson.Document;

/**
 * This construct represents the replaceOne method on MongoDB.
 *
 * @author Edward van Egdom
 */
public class ReplaceOneConstruct extends UpdateConstruct {
    @Override
    protected Argument[] getApiArguments() {
        return new Argument[]{
                new Argument("query", emptyObject(), OBJECT),
                new Argument("replacement", emptyObject(), OBJECT),
                new Argument("options", emptyObject(), OBJECT)
        };
    }

    @Override
    protected UpdateResult tryUpdate(MongoCollection<Document> collection, Document filter, Document replacement, UpdateOptions options) {
        UpdateResult result;
        try {
            result = collection.replaceOne(filter, replacement, options);
        } catch (com.mongodb.MongoException e) {
            throw new RobotRuntimeException("Update failed: " + e.getMessage(), e);
        }
        return result;
    }
}
