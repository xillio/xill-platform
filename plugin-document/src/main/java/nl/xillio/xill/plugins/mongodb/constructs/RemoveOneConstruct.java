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
import com.mongodb.client.result.DeleteResult;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.bson.Document;

/**
 * This construct represents the deleteOne method on MongoDB.
 *
 * @author Edward van Egdom
 */
public class RemoveOneConstruct extends RemoveConstruct {
    @Override
    protected MetaExpression processResult(DeleteResult result) {
        if (result.wasAcknowledged()) {
            return fromValue(result.getDeletedCount() > 0);
        } else {
            return fromValue(false);
        }
    }

    @Override
    protected DeleteResult tryDelete(MongoCollection<Document> collection, Document query) {
        DeleteResult result;
        try {
            result = collection.deleteOne(query);
        } catch (MongoException e) {
            throw new RobotRuntimeException("Remove failed: " + e.getMessage(), e);
        }
        return result;
    }
}
