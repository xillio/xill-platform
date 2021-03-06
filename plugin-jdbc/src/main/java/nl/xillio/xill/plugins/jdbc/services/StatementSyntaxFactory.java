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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * This interface represents an object that can generate a select query.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public interface StatementSyntaxFactory {
    String escapeString(String unescaped);

    String selectOne(String tableName, Map<String, Object> constraints);

    PreparedStatement selectOne(Connection connection, String tableName, Map<String, Object> constraints) throws SQLException;

    String insert(String tableName, Map<String, Object> values);

    PreparedStatement insert(Connection connection, String tableName, Map<String, Object> values) throws SQLException;

    String update(String tableName, Map<String, Object> values, Map<String, Object> constraints);

    PreparedStatement update(Connection connection, String tableName, Map<String, Object> values, Map<String, Object> constraints) throws SQLException;
}
