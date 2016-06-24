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

import me.biesaart.utils.StringUtils;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is responsible for building a select statement that will select rows based on equality constraints.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public class StatementSyntaxFactoryImpl implements StatementSyntaxFactory {

    @Override
    public String escapeString(String unescaped) {
        String escaped = unescaped;
        escaped = escaped.replaceAll("(\\\\|'|\")", "\\\\$1");
        escaped = escaped.replaceAll("\n", "\\\\n");
        escaped = escaped.replaceAll("\r", "\\\\r");
        escaped = escaped.replaceAll("\t", "\\\\t");
        escaped = escaped.replaceAll("\000", "\\\\000");
        return escaped;
    }

    @Override
    public String selectOne(String tableName, Map<String, Object> constraints) {
        String constraintString = constraints(constraints);
        String escapeTableName = escapeTableName(tableName);
        if (constraintString == null || constraintString.isEmpty()) {
            return selectOne(escapeTableName);
        }
        return selectOne(escapeTableName, constraintString);
    }

    @Override
    public PreparedStatement selectOne(Connection connection, String tableName, Map<String, Object> constraints) throws SQLException {
        String sql = selectOne(tableName, constraints);
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, constraints.values(), 1);
        return statement;
    }

    @Override
    public String insert(String tableName, Map<String, Object> values) {
        String valueString = parameterValueString(values.size());
        List<String> escapedColumns = values.keySet().stream().map(this::escapeColumnName).collect(Collectors.toList());
        String keyString = keyValueString(escapedColumns);
        String escapedTableName = escapeTableName(tableName);
        return insert(escapedTableName, keyString, valueString);
    }

    @Override
    public PreparedStatement insert(Connection connection, String tableName, Map<String, Object> values) throws SQLException {
        String sql = insert(tableName, values);
        PreparedStatement statement = connection.prepareStatement(sql);
        setParameters(statement, values.values(), 1);
        return statement;
    }

    @Override
    public String update(String tableName, Map<String, Object> values, Map<String, Object> constraints) {
        String escapedTableName = escapeTableName(tableName);
        String assignmentString = assignment(values);
        String constraintString = constraints(constraints);

        return update(escapedTableName, assignmentString, constraintString);
    }

    @Override
    public PreparedStatement update(Connection connection, String tableName, Map<String, Object> values, Map<String, Object> constraints) throws SQLException {
        String sql = update(tableName, values, constraints);
        PreparedStatement statement = connection.prepareStatement(sql);
        Collection<Object> setValues = values.values();
        Collection<Object> constraintValues = constraints.values();
        setParameters(statement, setValues, 1);
        setParameters(statement, constraintValues, 1 + setValues.size());
        return statement;
    }

    protected String update(String escapedTableName, String assignmentString, String constraintString) {
        return String.format("UPDATE %s SET %s WHERE %s", escapedTableName, assignmentString, constraintString);
    }

    protected String assignment(Map<String, Object> values) {
        List<String> parameters = values.keySet()
                .stream()
                .map(this::escapeColumnName)
                .map(this::addParameter)
                .collect(Collectors.toList());

        return StringUtils.join(parameters, ", ");
    }

    protected void setParameters(PreparedStatement statement, Collection<Object> parameters, int start) {
        int i = start;
        for (Object value : parameters) {
            try {
                if (value instanceof InputStream) {
                    statement.setBinaryStream(i++, (InputStream) value);
                } else {
                    statement.setObject(i++, value);
                }
            } catch (SQLException e) {
                throw new RobotRuntimeException(e.getMessage(), e);
            }
        }
    }

    private String keyValueString(List<String> strings) {
        return StringUtils.join(strings, ", ");
    }

    protected String insert(String escapedTableName, String keys, String values) {
        return String.format("INSERT INTO %s (%s) VALUES (%s)", escapedTableName, keys, values);
    }

    protected String constraints(Map<String, Object> constraints) {
        List<String> keys = constraints.keySet().stream()
                .map(this::escapeColumnName)
                .map(this::addParameter)
                .collect(Collectors.toList());

        return joinConditions(keys);
    }

    protected String parameterValueString(int count) {
        return StringUtils.repeat(parameterCharacter(), ", ", count);
    }

    protected String parameterCharacter() {
        return "?";
    }

    protected String addParameter(String escapedColumnName) {
        return escapedColumnName + "=" + parameterCharacter();
    }

    protected String joinConditions(List<String> conditions) {
        return StringUtils.join(conditions, " AND ");
    }

    protected String escapeTableName(String tableName) {
        return escapeIdentifier(tableName);
    }

    protected String escapeColumnName(String columnName) {
        return escapeIdentifier(columnName);
    }

    protected String escapeIdentifier(String unescaped) {
        return String.format("\"%s\"", unescaped);
    }

    protected String selectOne(String escapedTableName, String constraints) {
        return String.format("SELECT TOP 1 * FROM %s WHERE %s", escapedTableName, constraints);
    }

    protected String selectOne(String escapedTableName) {
        return "SELECT TOP 1 * FROM " + escapedTableName;
    }
}
