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

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.services.ExpressionConverter;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class is responsible for iterating over a result set and closing it once the close method is called.
 * This class is an implementation of the {@link java.util.Iterator} contract. It provides an iterator that will
 * walk through a {@link ResultSet} until it is done.
 * <p>
 * Every row of the {@link ResultSet} will be parsed to a {@link nl.xillio.xill.api.components.ExpressionDataType#OBJECT}
 * containing the column labels as keys and the extraction results from the {@link ExpressionConverter} as values.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public class ResultSetIterator extends MetaExpressionIterator<Object> {

    private static final Logger LOGGER = Log.get();
    private final ExpressionConverter expressionConverter;
    private final ResultSet resultSet;
    private final ResultSetMetaData metaData;
    private final List<String> columnNames;
    private final PreparedStatement statement;
    private Boolean hasNext = null;

    /**
     * Create a new iterator for a {@link ResultSet}
     *
     * @param expressionConverter Converter for JDBC objects to MetaExpressions
     * @param resultSet           The {@link ResultSet} to iterate over
     * @param statement           A {@link PreparedStatement} to close when iterating finishes
     * @throws SQLException When getting column metadata fails
     */
    public ResultSetIterator(ExpressionConverter expressionConverter, ResultSet resultSet, PreparedStatement statement) throws SQLException {
        super(null, null);
        this.expressionConverter = expressionConverter;
        this.resultSet = resultSet;
        this.metaData = resultSet.getMetaData();
        columnNames = new ArrayList<>();
        int count = metaData.getColumnCount();
        for (int i = 1; i <= count; i++) {
            columnNames.add(metaData.getColumnLabel(i));
        }
        this.statement = statement;
    }

    @Override
    public boolean hasNext() {
        if (hasNext == null) {
            try {
                hasNext = resultSet.next();
            } catch (SQLException e) {
                LOGGER.error("Failed to get next row", e);
                hasNext = false;
            }
        }
        return hasNext;
    }

    @Override
    public MetaExpression next() {
        if (!hasNext()) {
            throw new NoSuchElementException("The iterator is empty");
        }

        try {
            // Parse the result
            return expressionConverter.parseRow(resultSet, metaData);
        } catch (SQLException e) {
            throw new RobotRuntimeException("Could not parse row: " + e.getMessage(), e);
        } finally {
            // Set next status to unknown
            hasNext = null;
        }
    }

    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close result set", e);
        }
        try {
            statement.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close statement", e);
        }
    }
}
