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
package nl.xillio.xill.plugins.document.constructs;

import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.ConnectionFailedException;
import nl.xillio.xill.plugins.mongodb.services.BsonValueConverter;
import nl.xillio.xill.plugins.mongodb.services.ConnectionManager;
import nl.xillio.xill.plugins.mongodb.services.MongoConverter;
import nl.xillio.xill.plugins.mongodb.services.serializers.ObjectIdSerializer;
import org.bson.BsonValue;
import org.bson.Document;

/**
 * This construct represents an abstract implementation for all udm constructs that use the connection manager.
 *
 * @author Thomas Biesaart
 */
abstract class AbstractUDMConstruct extends Construct {
    private ConnectionManager connectionManager;
    private MongoConverter mongoConverter;
    private BsonValueConverter bsonValueConverter;
    private ObjectIdSerializer objectIdSerializer;

    @Inject
    void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Inject
    void setMongoConverter(MongoConverter mongoConverter) {
        this.mongoConverter = mongoConverter;
    }

    @Inject
    void setBsonValueConverter(BsonValueConverter bsonValueConverter) {
        this.bsonValueConverter = bsonValueConverter;
    }

    @Inject
    void setObjectIdSerializer(ObjectIdSerializer objectIdSerializer) {
        this.objectIdSerializer = objectIdSerializer;
    }

    protected MongoDatabase getDatabase(ConstructContext context, String identity) {
        try {
            return connectionManager.getConnectionByIdentity(context, identity).getDatabase();
        } catch (ConnectionFailedException e) {
            throw new RobotRuntimeException(
                    "Could not connect to the UDM database. Failed to establish the mongo connection.\n" +
                            "Reason: " + e.getMessage(),
                    e
            );
        }
    }

    protected MongoCollection<Document> getDocuments(ConstructContext constructContext, String identity) {
        return getDatabase(constructContext, identity).getCollection("documents");
    }

    protected <T> T performSafe(MongoOperationResult<T> operation) {
        try {
            return operation.perform();
        } catch (MongoException e) {
            throw new RobotRuntimeException(
                    "A mongo operation failed.\n" +
                            "Error Code: " + e.getCode() + "\n" +
                            "Reason: " + e.getMessage(),
                    e
            );
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(
                    "A mongo operation failed.\n" +
                            "Invalid Input: " + e.getMessage(),
                    e
            );
        }
    }

    protected void performSafe(MongoOperation operation) {
        performSafe(() -> {
            operation.perform();
            return null;
        });
    }

    protected Document toDocument(MetaExpression expression) {
        return mongoConverter.parse(expression);
    }

    protected Object toBsonValue(MetaExpression expression) {
        return MetaExpression.extractValue(expression, objectIdSerializer);
    }

    protected MetaExpression fromValue(Document document) {
        return mongoConverter.parse(document);
    }

    protected MetaExpression fromValue(BsonValue value) {
        return bsonValueConverter.convert(value);
    }

    protected MetaExpression fromObject(Object object) {
        if (object instanceof Document) {
            return fromValue((Document) object);
        }
        if (object instanceof BsonValue) {
            return fromValue((BsonValue) object);
        }
        return MetaExpression.parseObject(object, objectIdSerializer);
    }

    @FunctionalInterface
    protected interface MongoOperationResult<T> {
        T perform();
    }

    @FunctionalInterface
    protected interface MongoOperation {
        void perform();
    }
}
