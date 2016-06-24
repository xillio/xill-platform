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
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.bson.Document;

/**
 * This construct represents the updateOne method on MongoDB.
 *
 * @author Edward van Egdom
 */
public class UpdateOneConstruct extends UpdateConstruct {
    @Override
    protected MetaExpression processResult(UpdateResult result, ConstructContext context) {
        if (result.wasAcknowledged() && result.isModifiedCountAvailable()) {
            return fromValue(result.getModifiedCount() > 0);
        } else if (result.wasAcknowledged() && !result.isModifiedCountAvailable()) {
            context.getRootLogger().warn("Update succeeded, but update count is unavailable, due to server with Mongo version prior to 2.6");
        }
        return FALSE;
    }

    @Override
    protected UpdateResult tryUpdate(MongoCollection<Document> collection, Document filter, Document update, UpdateOptions options) {
        UpdateResult result;
        try {
            result = collection.updateOne(filter, update, options);
        } catch (com.mongodb.MongoException e) {
            throw new RobotRuntimeException("Update failed: " + e.getMessage(), e);
        }
        return result;
    }
}
