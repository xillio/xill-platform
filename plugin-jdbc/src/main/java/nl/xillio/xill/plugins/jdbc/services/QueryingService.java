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
import nl.xillio.xill.api.components.ExpressionBuilder;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This service is responsible for performing and parsing simple queries and handling all the errors around them.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class QueryingService {
    private final ExpressionConverter expressionConverter;

    @Inject
    public QueryingService(ExpressionConverter expressionConverter) {
        this.expressionConverter = expressionConverter;
    }

    /**
     * Execute a statement and return the first row as a MetaExpression.
     *
     * @param statement the statement
     * @return the expression
     */
    public MetaExpression singleResult(PreparedStatement statement) {
        return singleResult(
                statement,
                resultSet -> {
                    if (!resultSet.next()) {
                        return ExpressionBuilder.NULL;
                    }

                    return parseRow(resultSet);
                }
        );
    }

    /**
     * Execute a statement and check if it has any results.
     *
     * @param statement the statement
     * @return true if and only if the result has at least one row
     */
    public boolean hasResult(PreparedStatement statement) {
        return singleResult(
                statement,
                ResultSet::next
        );
    }

    /**
     * Execute a statement and catch SQLExceptions.
     * This method will also throw an exception if the result is not as expected
     *
     * @param statement       the statement
     * @param expectResultSet set this to true if the statement must result in a result set. If it shouldn't then set this to false
     * @throws RobotRuntimeException if an SQLException occurs or the result is not as specified by the expectResultSet parameter
     */
    public void requireExecute(PreparedStatement statement, boolean expectResultSet) {
        if (execute(statement) != expectResultSet) {
            // This should never happen
            throw new RobotRuntimeException(String.format("The query yield %s result set", expectResultSet ? "no" : "a"));
        }
    }

    /**
     * Execute a statement that must result in a ResultSet. After execution the parser callback will be called
     * to parse the result set into a result.
     *
     * @param statement the statement
     * @param parser    the parser callback
     * @param <T>       the return type
     * @return the parsed output
     */
    private <T> T singleResult(PreparedStatement statement, SQLFunction<T> parser) {
        requireExecute(statement, true);

        try (ResultSet resultSet = getResultSet(statement)) {
            return parser.apply(resultSet);
        } catch (SQLException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }

    private MetaExpression parseRow(ResultSet resultSet) {
        try {
            return expressionConverter.parseRow(resultSet);
        } catch (SQLException e) {
            throw new RobotRuntimeException("Could not parse result: " + e.getMessage(), e);
        }
    }

    private boolean execute(PreparedStatement statement) {
        try {
            return statement.execute();
        } catch (SQLException e) {
            throw new RobotRuntimeException("Failed to execute a query: " + e.getMessage(), e);
        }
    }

    private ResultSet getResultSet(Statement statement) {
        try {
            return statement.getResultSet();
        } catch (SQLException e) {
            throw new RobotRuntimeException("No result set found: " + e.getMessage(), e);
        }
    }

    private interface SQLFunction<T> {
        T apply(ResultSet resultSet) throws SQLException;
    }
}
