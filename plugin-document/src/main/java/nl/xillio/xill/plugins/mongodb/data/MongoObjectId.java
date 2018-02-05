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
package nl.xillio.xill.plugins.mongodb.data;

import nl.xillio.xill.api.components.CopyableMetadataExpression;
import org.bson.types.ObjectId;

/**
 * Represents a Mongo ObjectId
 *
 * @author Titus Nachbauer
 */
public class MongoObjectId implements CopyableMetadataExpression<MongoObjectId> {
    private final ObjectId objectId;

    /**
     * Creates a new MongoObjectId with the given ObjectId.
     */
    public MongoObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    /**
     * Creates a new ObjectId based on a 24 character hexadecimal string
     *
     * @param objectIdHex the id string
     */
    public MongoObjectId(String objectIdHex) {
        objectId = new ObjectId(objectIdHex);
    }

    @Override
    public String toString() {
        return objectId.toString();
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    @Override
    public MongoObjectId copy() {
        // ObjectID is immutable, so we can use the same instance
        return new MongoObjectId(objectId);
    }
}
