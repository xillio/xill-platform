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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This factory is responsible for building prepared statements from a query.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class StatementFactory {
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?<!\\\\):([a-zA-Z_\\d]+)");
    private final ExpressionConverter converter;

    @Inject
    public StatementFactory(ExpressionConverter converter) {
        this.converter = converter;
    }

    /**
     * Build a prepared statement using a given connection and query.
     * The query can contain parameters using either ? notation or :label notation. If parameters are provided using
     * an {@link nl.xillio.xill.api.components.ExpressionDataType#OBJECT} then the :label notation must be used.
     * Otherwise the ? notation is used.
     *
     * @param connection the connection that should run this statement
     * @param query      the sql query that should be executed
     * @param parameters the parameter expression
     * @param timeout    the timeout or {@link nl.xillio.xill.api.components.ExpressionBuilder#NULL}
     * @return the statement
     * @throws RobotRuntimeException if one of the parameters is invalid
     */
    public PreparedStatement build(Connection connection, MetaExpression query, MetaExpression parameters, MetaExpression timeout) {
        PreparedStatement statement = buildStatement(connection, query, parameters);
        setTimeout(timeout, statement);
        return statement;
    }

    private void setTimeout(MetaExpression timeout, PreparedStatement statement) {
        if (!timeout.isNull() && !Double.isNaN(timeout.getNumberValue().doubleValue())) {
            try {
                statement.setQueryTimeout(timeout.getNumberValue().intValue());
            } catch (SQLException e) {
                throw new RobotRuntimeException(e.getMessage(), e);
            }
        }
    }

    private PreparedStatement buildStatement(Connection connection, MetaExpression query, MetaExpression parameters) {
        switch (parameters.getType()) {
            case LIST:
                return buildList(connection, query, parameters.getValue());
            case OBJECT:
                return buildObject(connection, query, parameters.getValue());
            default:
                throw new RobotRuntimeException("The parameters must be provided as either a LIST or an OBJECT");
        }
    }

    private PreparedStatement buildStatement(Connection connection, String query) {
        try {
            return connection.prepareStatement(query);
        } catch (SQLException e) {
            throw new RobotRuntimeException("SQL Error: " + e.getMessage(), e);
        }
    }

    private PreparedStatement buildList(Connection connection, MetaExpression query, List<MetaExpression> parameters) {
        PreparedStatement statement = buildStatement(connection, query.getStringValue());
        int i = 1;
        for (MetaExpression value : parameters) {
            try {
                Object param = converter.extract(value);
                statement.setObject(i++, param);
            } catch (SQLException e) {
                throw new RobotRuntimeException(e.getMessage(), e);
            }
        }
        return statement;
    }

    private PreparedStatement buildObject(Connection connection, MetaExpression query, Map<String, MetaExpression> parameters) {
        String queryString = query.getStringValue();
        Matcher matcher = PARAMETER_PATTERN.matcher(queryString);
        List<Object> parameterValues = parseParameterValues(matcher, parameters);
        String sql = matcher.replaceAll("?");

        PreparedStatement statement = buildStatement(connection, sql);

        int i = 1;
        for (Object object : parameterValues) {
            try {
                if (object instanceof InputStream) {
                    statement.setBinaryStream(i++, (InputStream) object);
                } else {
                    statement.setObject(i++, object);
                }
            } catch (SQLException e) {
                throw new RobotRuntimeException("Could not parseExpression parameter: " + e.getMessage(), e);
            }
        }

        return statement;
    }

    private List<Object> parseParameterValues(Matcher matcher, Map<String, MetaExpression> parameters) {
        List<Object> parameterValues = new ArrayList<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            MetaExpression item = parameters.get(name);

            if (item == null) {
                throw new RobotRuntimeException("The query contained parameter `" + name + "` but this key was not present in the passed object");
            }

            parameterValues.add(converter.extract(item));
        }
        return parameterValues;
    }
}
