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
package nl.xillio.xill.plugins.database.services;

import nl.xillio.xill.plugins.database.util.Database;
import nl.xillio.xill.plugins.database.util.Tuple;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database service for MySQL
 *
 * @author Geert Konijnendijk
 * @author Sander Visser
 */
public class MysqlDatabaseServiceImpl extends BaseDatabaseService {

    @SuppressWarnings("unchecked")
    @Override
    public Connection createConnection(String database, String user, String pass, Tuple<String, String>... options) throws SQLException {
        return connect(createConnectionURL(database, user, pass, options));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected String createConnectionURL(String database, String user, String pass, Tuple<String, String>... options) {
        return createJDBCURL("mysql", database, user, pass, "?", "&", options);
    }

    @Override
    public void loadDriver() throws ClassNotFoundException {
        Class.forName(Database.MYSQL.getDriverClass());
    }
}
