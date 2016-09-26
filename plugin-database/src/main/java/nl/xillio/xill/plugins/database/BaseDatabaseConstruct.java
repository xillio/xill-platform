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
package nl.xillio.xill.plugins.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.XillThreadFactory;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import nl.xillio.xill.plugins.database.util.ConnectionMetadata;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * The base class for each construct in the database plugin.
 */
@Singleton
public abstract class BaseDatabaseConstruct extends Construct {

    private static final int VALIDATION_TIMEOUT = 1000;

    private static final Logger LOGGER = Log.get();

    private static LinkedHashMap<RobotID, ConnectionMetadata> lastConnections = new LinkedHashMap<>();

    @Inject
    protected DatabaseServiceFactory factory;

    @Inject
    public BaseDatabaseConstruct(XillThreadFactory xillThreadFactory) {
        registerShutdownHook(xillThreadFactory);
    }

    @Override
    public final ConstructProcessor prepareProcess(ConstructContext context) {
        // Before start check that previous connections can be used
        context.addRobotStartedListener(action -> cleanLastConnections());
        return doPrepareProcess(context);
    }

    /**
     * @param context The context of the construct
     * @return The {@link ConstructProcessor} that should be returned in {@link BaseDatabaseConstruct#prepareProcess(ConstructContext)}
     */
    protected abstract ConstructProcessor doPrepareProcess(ConstructContext context);

    /**
     * Add hook to close all connections when runtime terminates
     */
    public void registerShutdownHook(XillThreadFactory xillThreadFactory) {
        // Close all connections when the VM stops (used in the IDE)
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAllConnections));
        // Close all connections when the XillEnvironment stops (used in Xill Server)
        Thread closeConnectionsThread = xillThreadFactory.create(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    // Guard for spurious wakeups using a while loop
                    while (true) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    closeAllConnections();
                    Thread.currentThread().interrupt();
                }
            }
        }, "Database Plugin Connections Cleanup");
        closeConnectionsThread.setDaemon(true);
        closeConnectionsThread.start();
    }

    private void closeAllConnections() {
        lastConnections.forEach((k, v) -> closeConnection(v.getConnection()));
    }

    /**
     * Close a connection without checked exceptions
     *
     * @param c
     */
    private void closeConnection(Connection c) {
        try {
            c.close();
        } catch (SQLException e) {
            LOGGER.error("Could not close database connection", e);
        }
    }

    /**
     * Set the last made connection.
     *
     * @param id                 The ID of the connection.
     * @param connectionMetadata The connection.
     */
    public static void setLastConnection(RobotID id, ConnectionMetadata connectionMetadata) {
        lastConnections.put(id, connectionMetadata);
    }

    /**
     * Get the {@link ConnectionMetadata} which was last used given an ID.
     *
     * @param id The ID.
     * @return The last used ConnectionMetadata.
     */
    public static ConnectionMetadata getLastConnection(RobotID id) {
        // Determine that there is a connection that can be used
        if (!lastConnections.containsKey(id)) {
            throw new RobotRuntimeException("There is no connection that can be used, connect to a database first");
        }

        // Determine that this connection is usable
        ConnectionMetadata metadata = lastConnections.get(id);
        try {
            if (metadata.getConnection().isClosed()) {
                lastConnections.remove(id);
                throw new RobotRuntimeException("The last connection was closed, reconnect to a database");
            }
        } catch (SQLException e) {
            lastConnections.remove(id);
            throw new RobotRuntimeException("The last connection cannot be used: " + e.getMessage(), e);
        }

        return metadata;
    }

    /**
     * Check for validity of all lastConnections and remove the invalid ones.
     */
    public static void cleanLastConnections() {
        for (Entry<RobotID, ConnectionMetadata> entry : new ArrayList<>(lastConnections.entrySet())) {
            Connection connection = entry.getValue().getConnection();
            RobotID id = entry.getKey();
            try {
                // Try to find out if a connection is still valid, don't bother if this takes too long
                if (connection.isClosed() || !connection.isValid(VALIDATION_TIMEOUT)) {
                    lastConnections.remove(id);
                }
            } catch (SQLException e) {
                // When an operation on the connection fails also assume it is invalid
                LOGGER.debug("Operation on connection failed.", e);
                lastConnections.remove(id);
            }
        }
    }
}
