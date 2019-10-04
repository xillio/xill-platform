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
package nl.xillio.xill.plugins.jdbc.services;

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.data.ConnectionWrapper;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is responsible for building connections to a database system.
 * It should also keep track of the last created connection per execution. This means that if another class uses an
 * instance of the connection factory to build a connection, any other class will be able to fetch that connection given
 * the {@link ConstructContext}.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public abstract class ConnectionFactory {
    private static final Logger LOGGER = Log.get();
    private final Map<UUID, Connection> connections = new HashMap<>();

    /**
     * Build a connection and store it as the last created connection.
     * The created connection will be shut down as soon as the current context is interrupted or stopped.
     *
     * @param context          the context
     * @param connectionString the jdbc url for the connection
     * @param options          nullable additional options
     * @return the connection
     * @throws SQLException if a db error occurs
     */
    public Connection createAndStore(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException {
        UUID id = context.getCompilerSerialId();
        Connection connection = buildConnection(context, connectionString, options);
        if (connection == null) {
            throw new SQLException("Connection String is incorrect");
        }
        connections.put(id, connection);

        context.addRobotStoppedListener(e -> close(connection));
        context.addRobotInterruptListener(e -> close(connection));

        return connection;
    }

    /**
     * Get the last created connection for a specific context.
     *
     * @param context the context
     * @return the connection if it is available
     */
    public Optional<Connection> get(ConstructContext context) {
        return Optional.ofNullable(connections.get(context.getCompilerSerialId()));
    }

    /**
     * Get the connection from an expression or the cached connection.
     * If an override is provided and it contains a connection that connection will be returned.
     * Otherwise a connection from the cache will be returned.
     * If no cache was available this method will throw an exception.
     *
     * @param context  the context for the cache
     * @param override the override expression
     * @return the connection
     * @throws RobotRuntimeException if no connection was found in the override or the cache.
     */
    public Connection getOrError(ConstructContext context, MetaExpression override) {
        if (override == null || override.isNull() || !override.hasMeta(ConnectionWrapper.class)) {
            return get(context)
                    .orElseThrow(
                            () -> new RobotRuntimeException("No connection found, please connect to a database using the connect construct.")
                    );
        }

        return override.getMeta(ConnectionWrapper.class).getConnection();
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close SQL connection", e);
        }
    }

    protected abstract Connection buildConnection(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException;
}
