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

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class implements its own isValid method.
 * This is due to the fact that our JDBC driver (jTDS 1.3.1) uses JDBC 3.0.
 * JDBC 3.0 does not support the method isValid (JDBC 4.0 does).
 *
 * @author Pieter Dirk Soels
 * @author Thomas Biesaart
 */
public class MSSQLCompatibilityConnection extends DelegateConnection {

    private static final Logger LOGGER = Log.get();

    public MSSQLCompatibilityConnection(Connection delegate) {
        super(delegate);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (timeout < 0) {
            throw new SQLException("Timeout < 0");
        }
        try (
                Statement stmt = createStatement();
                ResultSet results = stmt.executeQuery("select 'a'")) {
            return results.next() && results.getObject(1) != null;
        } catch (SQLException e) {
            LOGGER.error("Connection validation failed. Connection is invalid", e);
            return false;
        }
    }
}
