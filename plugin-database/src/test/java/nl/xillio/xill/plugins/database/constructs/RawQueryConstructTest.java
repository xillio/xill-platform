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
package nl.xillio.xill.plugins.database.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.BaseDatabaseConstruct;
import nl.xillio.xill.plugins.database.services.DatabaseService;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import nl.xillio.xill.plugins.database.util.ConnectionMetadata;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link QueryConstruct}.
 */
public class RawQueryConstructTest extends TestUtils {

    /**
     * <p>
     * Tests the process with a database and two parameters handed
     * </p>
     *
     * @throws ReflectiveOperationException
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testProcessWithDatabaseAndNormalUsage() throws ReflectiveOperationException, SQLException {
        // mock

        // the query
        MetaExpression query = mockExpression(ATOMIC, true, 5, "QueryValue");

        // the database
        MetaExpression database = mockExpression(ATOMIC, true, 42, "database");
        ConnectionMetadata connectionMetadata = mock(ConnectionMetadata.class);
        when(database.getMeta(ConnectionMetadata.class)).thenReturn(connectionMetadata);

        // the timeout
        MetaExpression timeout = mockExpression(ATOMIC, true, 42, "timeout");

        // the parameters
        MetaExpression parameters = mock(MetaExpression.class);
        List<MetaExpression> parameterValue = new ArrayList<MetaExpression>();
        MetaExpression firstParameter = mock(MetaExpression.class);
        MetaExpression secondParameter = mock(MetaExpression.class);

        LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
        parameterValue.add(firstParameter);
        parameterValue.add(secondParameter);
        when(parameters.getValue()).thenReturn(parameterValue);
        when(firstParameter.getValue()).thenReturn(value);
        when(secondParameter.getValue()).thenReturn(value);
        when(firstParameter.getType()).thenReturn(OBJECT);
        when(secondParameter.getType()).thenReturn(OBJECT);
        when(parameters.getType()).thenReturn(LIST);

        // the databaseFactory
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        DatabaseService databaseService = mock(DatabaseService.class);
        when(factory.getService(anyString())).thenReturn(databaseService);

        // context
        ConstructContext context = mock(ConstructContext.class);

        // run
        QueryConstruct.process(query, parameters, database, timeout, factory, context);

        // verify
        verify(databaseService, times(1)).preparedQuery(any(), anyString(), anyList(), anyInt(), any());
    }

    /**
     * <p>
     * Test the process when no database is handed and it has to be retrieved from the static storage
     * </p>
     *
     * @throws ReflectiveOperationException
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testProcessWithNoDatabaseAndNormalUsage() throws ReflectiveOperationException, SQLException {
        // mock

        // the query
        MetaExpression query = mockExpression(ATOMIC, true, 5, "QueryValue");

        // the database
        MetaExpression database = mockExpression(ATOMIC, true, 42, "database");
        when(database.isNull()).thenReturn(true);
        ConnectionMetadata connectionMetadata = mock(ConnectionMetadata.class);
        Connection connection = mock(Connection.class);
        when(connection.isClosed()).thenReturn(false);
        when(connectionMetadata.getConnection()).thenReturn(connection);

        // the timeout
        MetaExpression timeout = mockExpression(ATOMIC, true, 42, "timeout");

        // the parameters
        MetaExpression parameters = mock(MetaExpression.class);
        List<MetaExpression> parameterValue = new ArrayList<MetaExpression>();
        MetaExpression firstParameter = mock(MetaExpression.class);
        MetaExpression secondParameter = mock(MetaExpression.class);

        LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
        parameterValue.add(firstParameter);
        parameterValue.add(secondParameter);
        when(parameters.getValue()).thenReturn(parameterValue);
        when(firstParameter.getValue()).thenReturn(value);
        when(secondParameter.getValue()).thenReturn(value);
        when(firstParameter.getType()).thenReturn(OBJECT);
        when(secondParameter.getType()).thenReturn(OBJECT);
        when(parameters.getType()).thenReturn(LIST);

        // the databaseFactory
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        DatabaseService databaseService = mock(DatabaseService.class);
        when(factory.getService(anyString())).thenReturn(databaseService);

        // the robotID
        RobotID robotID = mock(RobotID.class);
        BaseDatabaseConstruct.setLastConnection(robotID, connectionMetadata);

        // context
        ConstructContext context = mock(ConstructContext.class);
        when(context.getRootRobot()).thenReturn(robotID);

        // run
        QueryConstruct.process(query, parameters, database, timeout, factory, context);

        // verify
        verify(databaseService, times(1)).preparedQuery(any(), anyString(), anyList(), anyInt(), any());
    }

    /**
     * @return Returns all possible exceptions when trying to initiate the service.
     */
    @DataProvider(name = "serviceExceptions")
    private Object[][] getAllServiceExceptions() {
        return new Object[][]{
                {null, new InstantiationException()},
                {null, new IllegalAccessException()},
                {null, new ClassNotFoundException()}
        };
    }

    /**
     * <p>
     * Tests the process when the getService throws an exception
     * </p>
     * <p>
     * Note that we hand every possible exception with a {@link DataProvider}
     * </p>
     *
     * @param o A random fodder object.
     * @param e The exception we throw.
     * @throws ReflectiveOperationException
     * @throws SQLException
     */
    @Test(dataProvider = "serviceExceptions", expectedExceptions = RobotRuntimeException.class)
    public void testProcessWithDatabaseWithFailingToGetService(final Object o, final Exception e) throws ReflectiveOperationException, SQLException {
        // mock

        // the query
        MetaExpression query = mockExpression(ATOMIC, true, 5, "QueryValue");

        // the database
        MetaExpression database = mockExpression(ATOMIC, true, 42, "database");
        when(database.isNull()).thenReturn(false);

        // the timeout
        MetaExpression timeout = mockExpression(ATOMIC, true, 42, "timeout");

        // the parameters
        MetaExpression parameters = mock(MetaExpression.class);
        List<MetaExpression> parameterValue = new ArrayList<MetaExpression>();
        MetaExpression firstParameter = mock(MetaExpression.class);

        LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
        parameterValue.add(firstParameter);
        when(parameters.getValue()).thenReturn(parameterValue);
        when(firstParameter.getValue()).thenReturn(value);
        when(firstParameter.getType()).thenReturn(OBJECT);
        when(parameters.getType()).thenReturn(LIST);

        // the databaseFactory
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        when(factory.getService(anyString())).thenThrow(e);

        // context
        ConstructContext context = mock(ConstructContext.class);

        // run
        QueryConstruct.process(query, parameters, database, timeout, factory, context);
    }

    /**
     * @return Returns all possible exceptions when trying to execute the query method
     */
    @DataProvider(name = "queryExceptions")
    private Object[][] getAllQueryExceptions() {
        return new Object[][]{
                {null, new SQLException()},
                {null, new IllegalArgumentException()}
        };
    }

    /**
     * <p>
     * tests the process when the query function fails and throws an exception
     * </p>
     * <p>
     * Note that we try every possible exception with a {@link DataProvider}.
     * </p>
     *
     * @param o A fodder object.
     * @param e The exception we throw.
     * @throws ReflectiveOperationException
     * @throws SQLException
     */
    @SuppressWarnings({"unchecked"})
    @Test(dataProvider = "queryExceptions", expectedExceptions = RobotRuntimeException.class)
    public void testProcessWithDatabaseAndQueryFailure(final Object o, final Exception e) throws ReflectiveOperationException, SQLException {
        // mock

        // the query
        MetaExpression query = mockExpression(ATOMIC, true, 5, "QueryValue");

        // the database
        MetaExpression database = mockExpression(ATOMIC, true, 42, "database");
        ConnectionMetadata connectionMetadata = mock(ConnectionMetadata.class);
        when(database.getMeta(ConnectionMetadata.class)).thenReturn(connectionMetadata);

        // the timeout
        MetaExpression timeout = mockExpression(ATOMIC, true, 42, "timeout");

        // the parameters
        MetaExpression parameters = mock(MetaExpression.class);
        List<MetaExpression> parameterValue = new ArrayList<MetaExpression>();
        MetaExpression firstParameter = mock(MetaExpression.class);
        MetaExpression secondParameter = mock(MetaExpression.class);

        LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
        parameterValue.add(firstParameter);
        parameterValue.add(secondParameter);
        when(parameters.getValue()).thenReturn(parameterValue);
        when(firstParameter.getValue()).thenReturn(value);
        when(secondParameter.getValue()).thenReturn(value);
        when(firstParameter.getType()).thenReturn(OBJECT);
        when(secondParameter.getType()).thenReturn(OBJECT);
        when(parameters.getType()).thenReturn(LIST);

        // the databaseFactory
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        DatabaseService databaseService = mock(DatabaseService.class);
        when(factory.getService(anyString())).thenReturn(databaseService);
        when(databaseService.preparedQuery(any(), anyString(), anyList(), anyInt(), any())).thenThrow(e);

        // context
        ConstructContext context = mock(ConstructContext.class);

        // run
        QueryConstruct.process(query, parameters, database, timeout, factory, context);
    }

    /**
     * Tests the returnValue method with an integer handed.
     */
    @Test
    public void testReturnValueWithInteger() {
        MetaExpression output = QueryConstruct.returnValue(42, "sql");

        Assert.assertEquals(output, fromValue(42));
    }

    /**
     * Tests the returnValue method with an invalid value handed.
     */
    @Test
    public void testReturnValueWithNoValidValue() {
        MetaExpression output = QueryConstruct.returnValue(null, "sql");

        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the returnValue method with an iterator with integers handed.
     */
    @Test
    public void testReturnValueWithIntegerIterator() {
        Iterator<Integer> iterator = Arrays.asList(5, 10).iterator();

        MetaExpression output = QueryConstruct.returnValue(iterator, "sql");

        Assert.assertEquals(output.getStringValue(), "Results[sql]");
    }

    /**
     * Test the transformIteratorElement method when it receives an integer.
     */
    @Test
    public void testTransformIteratorElementWithInteger() {

        MetaExpression output = QueryConstruct.transformIteratorElement(42);

        Assert.assertEquals(output, fromValue(42));
    }

    /**
     * Test the transformIteratorElement method when it receives a HashMap.
     */
    @Test
    public void testTransformIteratorElementWithHashMap() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("key", "value");

        MetaExpression output = QueryConstruct.transformIteratorElement(map);

        Assert.assertEquals(output.getValue().toString(), "{key=\"value\"}");
    }
}
