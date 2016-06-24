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
package nl.xillio.xill.plugins.mongodb.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoTimeoutException;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.ConnectionFailedException;
import nl.xillio.xill.plugins.mongodb.NoSuchConnectionException;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * This class is responsible for the management of MongoDB {@link Connection connections}.
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
@Singleton
public class ConnectionManager {
    private static final Logger LOGGER = Log.get();
    /**
     * This cache keeps track of the last created connection in a running instance.
     */
    private final Map<UUID, Connection> connectionCache = new ConcurrentHashMap<>();
    /**
     * This map keeps track of the connection info used to build a connection.
     */
    private final Map<Connection, ConnectionInfo> connectionInfoMap = new ConcurrentHashMap<>();
    private final ConnectionFactory connectionFactory;

    /**
     * Create a new ConnectionManager.
     *
     * @param connectionFactory the factory that should be used to create connections
     */
    @Inject
    public ConnectionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Connection getConnectionByIdentity(ConstructContext context, String identity) throws ConnectionFailedException {
        //find connection by identity
        Optional<Connection> foundConnection = findConnectionByIdentity(identity);

        return foundConnection.isPresent() ? foundConnection.get() : getConnection(context, new ConnectionInfo(identity));
    }

    private Optional<Connection> findConnectionByIdentity(String identity) {
        for (Map.Entry<Connection, ConnectionInfo> entry : connectionInfoMap.entrySet()) {
            if (entry.getValue().getIdentity().equals(identity)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * Get an open cached connection or create if it does not exist.
     *
     * @param context the context for the connection
     * @param info    the connection information
     * @return the connection
     * @throws ConnectionFailedException if the connection fails
     * @see ConnectionManager#getConnection(ConstructContext)
     */
    public Connection getConnection(ConstructContext context, ConnectionInfo info) throws ConnectionFailedException {
        synchronized (connectionCache) {
            Connection connection = getOpen(context);

            // Check if the connection info is different
            if (connection != null) {
                ConnectionInfo initInfo = connectionInfoMap.get(connection);
                if (!initInfo.equals(info)) {
                    // Pretend we don't have a connection yet because the connection info is different
                    connection = null;
                }
            }

            if (connection == null) {
                LOGGER.info("Creating connection for {}", info);
                // We have to create a new connection
                connection = connectionFactory.build(info);

                // Validate the connection
                validate(connection);

                connectionCache.put(context.getCompilerSerialId(), connection);
                connectionInfoMap.put(connection, info);

                // Add a listener to close the connection
                Connection connectionReference = connection;
                context.addRobotStoppedListener(action -> {
                    connectionReference.close();
                    clean();
                });
            }

            return connection;
        }
    }

    private void validate(Connection connection) throws ConnectionFailedException {
        try {
            connection.requireValid();
        } catch (MongoTimeoutException e) {
            throw new ConnectionFailedException("Could not connect to mongodb", e);
        }
    }

    /**
     * Clean the manager maps to make them ready for garbage collection.
     */
    private void clean() {
        LOGGER.info("Cleaning up connections");

        // Remove all closed elements from the cache
        List<UUID> closed = connectionCache.entrySet().stream()
                .filter(e -> e.getValue().isClosed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        closed.forEach(connectionCache::remove);

        // Remove all connection info mappings for connections that do not exist
        Collection<Connection> availableConnections = connectionCache.values();

        List<Connection> toBeRemoved = connectionInfoMap.keySet().stream()
                .filter(c -> !availableConnections.contains(c))
                .collect(Collectors.toList());

        toBeRemoved.forEach(connectionInfoMap::remove);
    }

    /**
     * Get a cached connection.
     *
     * @param context the context for which to fetch the connection
     * @return the connection
     * @throws NoSuchConnectionException if no connection is available
     */
    public Connection getConnection(ConstructContext context) throws NoSuchConnectionException {
        Connection connection = getOpen(context);

        if (connection == null) {
            throw new NoSuchConnectionException("No connection found for the given context");
        }

        return connection;
    }

    private Connection getOpen(ConstructContext context) {
        Connection connection = connectionCache.get(context.getCompilerSerialId());
        if (connection == null || connection.isClosed()) {
            return null;
        }
        return connection;
    }
}
