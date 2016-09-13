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
package nl.xillio.xill.plugins.database.util;

import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.util.TypeConverter.ConversionException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Iterates over a {@link Statement}. When the current result of the Statement is a {@link ResultSet},
 * the iterator returns a {@link Map} with table column labels (the value of an AS clause if present, else the column name)
 * as key and row values as values.
 * When the current result is an update count it returns a single integer.
 * The iterator will have a next value until there are no more results in the statement.
 *
 * @author Geert Konijnendijk
 * @author Sander Visser
 */
public class StatementIterator implements Iterator<Object>, AutoCloseable {

    private Statement statement;

    private ResultSet currentSet;
    private ResultSetMetaData currentMeta;
    private int currentUpdateCount = -1;
    private boolean hasNext;

    /**
     * Create an iterator over all results of a {@link Statement}. When iterating is finished the statement is closed.
     *
     * @param statement The statement to iterate over
     */
    public StatementIterator(Statement statement) {
        this.statement = statement;
        retrieveNextResult(true);
        hasNext = currentSet != null || currentUpdateCount != -1;
    }

    /**
     * Sets the currentUpdateCount or the currentSet depending on the current result of the statement.
     */
    @SuppressWarnings("squid:S2095") //currentSet is closed in iterator close() method
    void retrieveNextResult(boolean resultSetPossible) {
        try {
            if (resultSetPossible) {
                currentSet = statement.getResultSet();
            }
            if (currentSet == null) {
                currentUpdateCount = statement.getUpdateCount();
            } else {
                advance();
                if (currentSet != null) {
                    currentMeta = currentSet.getMetaData();
                }
            }
        } catch (SQLException e) {
            throw new StatementIterationException(e);
        }
    }

    /**
     * Moves to the statement's next result and retrieves it
     */
    void nextResult() {
        currentSet = null;
        currentUpdateCount = -1;
        try {
            boolean resultSet = statement.getMoreResults();
            retrieveNextResult(resultSet);
            // If the next result is no result and no update count, iterating has finished
            hasNext = currentSet != null || currentUpdateCount != -1;
            if (!hasNext)
                statement.close();
        } catch (SQLException e) {
            throw new StatementIterationException(e);
        }
    }

    /**
     * Sets the update count, needed for testing purposes.
     *
     * @param value The value we want the currentUpdateCount to have
     */
    void setCurrentUpdateCount(int value) {
        currentUpdateCount = value;
    }

    /**
     * Sets the currentMeta variable, needed for testing purposes.
     *
     * @param metadata The value we want to currentMeta to have.
     */
    void setCurrentMeta(ResultSetMetaData metadata) {
        currentMeta = metadata;
    }

    /**
     * Sets the currentSet, needed for testing purposes.
     *
     * @param resultSet The value we want currentSet to have.
     */
    void setCurrentSet(ResultSet resultSet) {
        currentSet = resultSet;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public Object next() {
        if (!hasNext())
            throw new NoSuchElementException("Iterator is empty");
        if (currentUpdateCount != -1) {
            int result = currentUpdateCount;
            // Immediately move to the next result since there's only one update count per result
            nextResult();
            return result;
        } else {
            try {

                Map<String, Object> result = new LinkedHashMap<>();

                // Build the resulting map using all column labels
                for (int i = 1; i <= currentMeta.getColumnCount(); i++) {
                    String columnLabel = currentMeta.getColumnLabel(i);
                    Object obj = currentSet.getObject(columnLabel);
                    Object converted;
                    converted = TypeConverter.convertJDBCType(obj);
                    result.put(columnLabel, converted);
                }

                advance();

                return result;
            } catch (SQLException | ConversionException e) {
                throw new RobotRuntimeException("An error has occurred while iterating: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ").", e);
            }
        }
    }

    /**
     * Move to the next result in the current {@link ResultSet} or to the next {@link ResultSet} if the current one is finished.
     *
     * @throws SQLException
     */
    private void advance() throws SQLException {
        // Advance the ResultSet
        currentSet.next();

        // Move to the next result when the set is empty
        if (currentSet.isAfterLast() || currentSet.getRow() == 0)
            nextResult();
    }

    /**
     * Ensure to close database prepared statement and result set objects when the iterator is about to be disposed.
     * This avoids memory leaks.
     *
     * @throws SQLException when error occurs during releasing resources
     */
    @Override
    public void close() throws SQLException {
        if (statement != null) {
            statement.close();
        }

        if (currentSet != null) {
            currentSet.close();
        }
    }

    /**
     * Thrown when a problem arises while iterating over a {@link Statement} in a {@link StatementIterator}.
     *
     * @author Geert Konijnendijk
     * @author Sander Visser
     */
    @SuppressWarnings("javadoc")
    public static class StatementIterationException extends RuntimeException {

        private static final long serialVersionUID = -2585302170331144436L;

        public StatementIterationException() {
            super();
        }

        public StatementIterationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }

        public StatementIterationException(String message, Throwable cause) {
            super(message, cause);
        }

        public StatementIterationException(String message) {
            super(message);
        }

        public StatementIterationException(Throwable cause) {
            super(cause);
        }

    }
}
