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
package nl.xillio.xill.plugins.jdbc.data;

import nl.xillio.xill.api.data.MetadataExpression;

import java.sql.Connection;

/**
 * This class represents a simple wrapper around a jdbc connection that allows moving it as a payload.
 * Note that this wrapper does not provide a close method to close the connection. This is the responsibility of the
 * {@link nl.xillio.xill.plugins.jdbc.services.ConnectionFactory} as all connection should be closed at end of execution
 * and not end of scope.
 * <p>
 * This requirement originates from the fact the connections should be cached so you do not have to pass the connection
 * variable to every construct if you are using only a single connection anyway.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public class ConnectionWrapper implements MetadataExpression {
    private final Connection connection;

    public ConnectionWrapper(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
