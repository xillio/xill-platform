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
package nl.xillio.xill.plugins.document.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.ConnectionFailedException;
import nl.xillio.xill.plugins.mongodb.services.ConnectionManager;
import nl.xillio.xill.plugins.mongodb.services.MongoConverter;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * This service is responsible for managing and caching the content types.
 * This service will cache every decorator and content type that is requested from it. Whenever saveDecorator or
 * saveContentType is called the decorator or content type that belongs to that call will be removed from the cache if
 * it existed. The next time the decorator is requested it will be cached again.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class ContentTypeService {
    private final ConnectionManager connectionManager;
    private final MongoConverter mongoConverter;
    private final DecoratorValidator decoratorValidator;
    private Map<String, Document> decoratorCache = new ConcurrentHashMap<>();
    private Map<String, Document> contentTypeCache = new ConcurrentHashMap<>();

    @Inject
    public ContentTypeService(ConnectionManager connectionManager, MongoConverter mongoConverter, DecoratorValidator decoratorValidator) {
        this.connectionManager = connectionManager;
        this.mongoConverter = mongoConverter;
        this.decoratorValidator = decoratorValidator;
    }

    /**
     * Store a decorator definition in the persistence that matches the given identity.
     * Calling this method will remove the decorator from the cache if it is present until
     * {@link #getDecorator(ConstructContext, String, String)} is called again for the same identity and decorator.
     *
     * @param context    the construct context for which to get the database connection
     * @param identity   the identity of the persistence
     * @param name       the name of the decorator
     * @param definition the definition that should be validated and saved
     * @throws RobotRuntimeException if the decorator definition is not valid
     * @throws RobotRuntimeException if no connection could be established to the persistence
     */
    public void saveDecorator(ConstructContext context, String identity, String name, MetaExpression definition) {
        decoratorValidator.validate(definition);
        getDecoratorsCollection(context, identity).replaceOne(
                new Document("_id", name),
                new Document("_id", name).append(name, mongoConverter.parse(definition)),
                new UpdateOptions().upsert(true)
        );
        decoratorCache.remove(key(identity, name));
    }

    /**
     * Store a content type in the persistence that matches the given identity.
     * Calling this method will remove the content type from the cache if it is present until
     * {@link #getContentType(ConstructContext, String, String)}  is called again for the same identity and content type.
     *
     * @param context    the construct context for which to get the database connection
     * @param identity   the identity of the persistence
     * @param name       the name of the decorator
     * @param decorators the names of the decorators that form this content type
     * @throws RobotRuntimeException if one of the decorators could not be found
     * @throws RobotRuntimeException if no connection could be established to the persistence
     */
    public void saveContentType(ConstructContext context, String identity, String name, List<String> decorators) {
        // Check if all decorators are defined
        for (String decoratorName : decorators) {
            getDecorator(context, identity, decoratorName)
                    .orElseThrow(() -> new RobotRuntimeException(
                            "No decorator definition was found for `" + decoratorName + "`"
                    ));
        }

        getContentTypesCollection(context, identity).replaceOne(
                new Document("_id", name),
                new Document("_id", name).append(name, decorators),
                new UpdateOptions().upsert(true)
        );
        contentTypeCache.remove(key(identity, name));
    }

    /**
     * Get a decorator from the persistence.
     * If the decorator is present in the cache it will be pulled from there.
     *
     * @param context  the context for the persistence
     * @param identity the persistence identity
     * @param name     the decorator name
     * @return the decorator definition if it was found
     * @throws RobotRuntimeException if no connection to the persistence could be established
     */
    public Optional<Document> getDecorator(ConstructContext context, String identity, String name) {
        return get(
                decoratorCache,
                identity,
                name,
                () -> getDecoratorsCollection(context, identity)
        );
    }

    /**
     * Get a content type from the persistence.
     * If the content type is present in the cache it will be pulled from there.
     *
     * @param context  the context for the persistence
     * @param identity the persistence identity
     * @param name     the content type name
     * @return the content type definition if it was found
     * @throws RobotRuntimeException if no connection to the persistence could be established
     */
    public Optional<List<String>> getContentType(ConstructContext context, String identity, String name) {
        return get(
                contentTypeCache,
                identity,
                name,
                () -> getContentTypesCollection(context, identity)
        );
    }


    private <T> Optional<T> get(Map<String, Document> cache,
                                String identity,
                                String name,
                                Supplier<MongoCollection<Document>> collectionSupplier) {

        // Check the cache
        String key = key(identity, name);
        if (cache.containsKey(key)) {
            Document item = cache.get(key);
            return Optional.ofNullable((T) item.get(name));
        }
        // Fetch decorator
        Document result = collectionSupplier.get()
                .find(new Document("_id", name))
                .first();

        if (result == null) {
            // Put a document without a definition to prevent calling the database again.
            result = new Document("_id", name);
        }
        cache.put(key, result);


        return Optional.ofNullable((T) result.get(name));
    }

    private MongoCollection<Document> getDecoratorsCollection(ConstructContext constructContext, String identity) {
        return getDatabase(constructContext, identity).getCollection("decoratorDefinitions");
    }

    private MongoCollection<Document> getContentTypesCollection(ConstructContext constructContext, String identity) {
        return getDatabase(constructContext, identity).getCollection("contentTypes");
    }

    private MongoDatabase getDatabase(ConstructContext constructContext, String identity) {
        try {
            return connectionManager.getConnectionByIdentity(constructContext, identity).getDatabase();
        } catch (ConnectionFailedException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * This method generates a unique string for a definition in a persistence.
     *
     * @param identity the persistence identity
     * @param name     the name of the decorator or content type
     * @return the key
     */
    private static String key(String identity, String name) {
        return String.format("KEY:%s:%s", identity, name);
    }
}
