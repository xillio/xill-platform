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
package nl.xillio.xill.plugins.database.util;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.MetadataExpression;

import java.sql.Connection;

/**
 * Data class for storing a {@link Connection} in a {@link MetaExpression}.
 *
 * @author Geert Konijnendijk
 * @author Sander Visser
 */
public class ConnectionMetadata implements MetadataExpression {

    String databaseName;
    private Connection connection;

    /**
     * Constructor for the ConnectionMetadata.
     *
     * @param databaseName The name of the database.
     * @param connection   The connection.
     */
    public ConnectionMetadata(String databaseName, Connection connection) {
        super();
        this.databaseName = databaseName;
        this.connection = connection;
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Sets the connection.
     *
     * @param newConnection The connection we want to set.
     */
    public void setConnection(Connection newConnection) {
        connection = newConnection;
    }

    /**
     * @return Returns the database name.
     */
    public String getDatabaseName() {
        return databaseName;
    }
}
