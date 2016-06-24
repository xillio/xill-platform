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
import nl.xillio.xill.plugins.database.util.StatementIterator.StatementIterationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * Test the {@link StatementIterator}
 */
public class StatementIteratorTest {

    /**
     * Test the retireveNextResult method when no ResultSet is handed.
     */
    @Test
    public void testRetrieveNextResultWithNoResultSet() {
        // mock
        Statement statement = mock(Statement.class);
        StatementIterator iterator = new StatementIterator(statement);

        // run
        iterator.retrieveNextResult(true);
    }

    /**
     * test the retrieveNextResult method when a ResultSet is handed.
     *
     * @throws SQLException
     */
    @Test
    public void testRetrieveNextResultWithResultSet() throws SQLException {
        // mock
        Statement statement = mock(Statement.class);
        ResultSet currentSet = mock(ResultSet.class);
        when(statement.getResultSet()).thenReturn(currentSet);
        StatementIterator iterator = new StatementIterator(statement);

        // run
        iterator.retrieveNextResult(true);
    }

    /**
     * test the retrieveNextResult method when the ResultSet throws an error.
     *
     * @throws SQLException
     */
    @Test(expectedExceptions = StatementIterationException.class)
    public void testRetrieveNextResultWithAnErrorThrown() throws SQLException {
        // mock
        Statement statement = mock(Statement.class);
        ResultSet currentSet = mock(ResultSet.class);
        when(statement.getResultSet()).thenReturn(currentSet);
        when(currentSet.next()).thenThrow(new SQLException());
        StatementIterator iterator = new StatementIterator(statement);

        // run
        iterator.retrieveNextResult(true);
    }

    /**
     * Test the nextResult method.
     */
    @Test
    public void testNextResultNormalUsage() {
        Statement statement = mock(Statement.class);
        StatementIterator iterator = spy(new StatementIterator(statement));

        when(iterator.hasNext()).thenReturn(true);
        iterator.nextResult();
    }

    /**
     * Test the next method with a current update
     */
    @Test
    public void testNextWithCurrentUpdate() {
        // mock
        Statement statement = mock(Statement.class);
        StatementIterator iterator = spy(new StatementIterator(statement));
        iterator.setCurrentUpdateCount(42);

        Object output = iterator.next();

        Assert.assertEquals(output, 42);
    }

    /**
     * Test the next method with no current update
     *
     * @throws SQLException
     */
    @Test
    public void testNextWithNoCurrentUpdate() throws SQLException {
        // mock
        Statement statement = mock(Statement.class);

        // the iterator
        StatementIterator iterator = spy(new StatementIterator(statement));
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnLabel(1)).thenReturn("ColumnName");
        iterator.setCurrentMeta(metadata);
        iterator.setCurrentUpdateCount(-1);
        iterator.setCurrentSet(resultSet);

        Object output = iterator.next();

        LinkedHashMap<String, Object> expectedResult = new LinkedHashMap<String, Object>();
        expectedResult.put("ColumnName", null);

        Assert.assertEquals(output, expectedResult);
    }

    /**
     * Test the next method with no current update and SQL failure
     *
     * @throws SQLException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNextWithNoCurrentUpdateAndSQLException() throws SQLException {
        // mock
        Statement statement = mock(Statement.class);

        // the iterator
        StatementIterator iterator = spy(new StatementIterator(statement));
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(metadata.getColumnCount()).thenThrow(new SQLException());
        iterator.setCurrentMeta(metadata);
        iterator.setCurrentUpdateCount(-1);

        Object output = iterator.next();

        Assert.assertEquals(output, 0);
    }
}
