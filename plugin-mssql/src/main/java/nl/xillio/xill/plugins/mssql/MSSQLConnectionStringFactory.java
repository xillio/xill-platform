/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.mssql;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.jdbc.services.ConnectionStringFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;


/**
 * This is the simple connection factory for mssql databases. It fetched the driver and uses jdbc to parseExpression the connection string.
 *
 * @author Thomas Biesaart
 * @author Pieter Dirk Soels
 */
class MSSQLConnectionStringFactory extends ConnectionStringFactory {

    @Override
    protected Class<SQLServerDriver> driver() {
        return SQLServerDriver.class;
    }

    @Override
    protected Connection buildConnection(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException {
        connectionString = parseConnectionString(connectionString);
        return super.buildConnection(context, connectionString, options);
    }

    /**
     * Parses the connection string to be compatible with the used MSSQL driver.
     * <p>
     * Previously a JTDS driver was used which imposed requirements on the connection string. As we moved to a
     * Microsoft driver instead, we need to parse the connection string to be backwards-compatible.
     * <p>
     * The changes between the two driver is that the JTDS driver required the protocol to be
     * {@code jdbc:jtds:sqlserver}, while Microsoft requires {@code jdbc:sqlserver}. Furthermore, JTDS used the
     * path part of the URL to denote the database, while Microsoft uses a property instead.
     * <p>
     * This method parses the connection string to allow both methods to be functional.
     *
     * @param connectionString the connection String to parse
     * @return the parsed connection String
     */
    String parseConnectionString(String connectionString) {
        // We can't easily parse the string to a URL/URI due to the 'invalid' protocol, furthermore properties are not
        // defined either, hence the string parsing here.
        int protocolIndex = connectionString.indexOf("://");
        String protocol = connectionString.substring(0, protocolIndex);
        if (protocol.contains("jtds")) {
            protocol = protocol.replace(":jtds", "");
            connectionString = protocol.concat(connectionString.substring(protocolIndex));
        }

        protocolIndex = connectionString.indexOf("://") + 3;
        int pathIndex = connectionString.indexOf('/', protocolIndex);
        int propertyIndex = connectionString.indexOf(';');
        if (pathIndex > protocolIndex && pathIndex < propertyIndex) {
            // There is a path part between the host and the properties, convert that to the database property
            String database = connectionString.substring(pathIndex, propertyIndex);
            String databaseProperty = ";database=".concat(database.substring(1)); // remove preceding /
            connectionString = connectionString.substring(0, pathIndex)
                    .concat(databaseProperty)
                    .concat(connectionString.substring(propertyIndex));
        }

        return connectionString;
    }
}
