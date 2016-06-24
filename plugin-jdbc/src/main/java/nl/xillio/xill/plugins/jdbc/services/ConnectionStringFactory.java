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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * This {@link ConnectionFactory} will build connections by loading a driver and parsing a connection string.
 * Subclasses of this factory solely have to provide a driver.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public abstract class ConnectionStringFactory extends ConnectionFactory {
    public ConnectionStringFactory() {
        try {
            Class.forName(driver().getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver " + driver().getName() + " was not loaded", e);
        }
    }

    @Override
    protected Connection buildConnection(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException {
        Properties properties = new Properties();

        // Load the properties
        if (options != null) {
            options.forEach((s, expression) -> properties.put(s, expression.getStringValue()));
        }

        return DriverManager.getConnection(connectionString, properties);
    }

    protected abstract Class<? extends Driver> driver();
}
