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

import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.util.Tuple;
import nl.xillio.xill.plugins.database.util.TypeConverter.ConversionException;
import org.mockito.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNull;

/**
 * Test the {@link BaseDatabaseService}.
 */
public class BaseDatabaseServiceTest {

    /**
     * method to check whether updateObject() is called when keys.size != 0 and overwrite is true
     *
     * @throws SQLException
     */
    @Test
    public void testStoreObjectDoUpdate() throws SQLException {

        // mock
        BaseDatabaseService bds = mock(BaseDatabaseService.class);
        Connection connection = mock(Connection.class);
        @SuppressWarnings("unchecked")
        ArrayList<String> keys = mock(ArrayList.class);

        LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
        obj.put("testKey", "testObj");

        // doThrow(new RuntimeException()).when(bds).updateObject(connection, "test", obj, keys);
        when(keys.size()).thenReturn(1);

        // run
        bds.storeObject(connection, "testKey", obj, keys, true);

        // verify
        // verify(bds,times(1)).updateObject(connection, "test", obj, keys);

        // assert
        // Nothing to assert.
    }

    /**
     * <p>
     * Tests the query method when:
     * </p>
     * <ul>
     * <li>There is only one parameter</li>
     * <li>This parameter creates a result where the first getUpdateCount returns -1</li>
     * </ul>
     *
     * @throws SQLException
     */
    @Test
    public void testQueryWithOneParameterAndNoFirstCount() throws SQLException {
        // Mock the input
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("param", "parameterValue");

        // Mock the process
        when(service.parseNamedParameters(connection, "query")).thenReturn(statement);
        when(statement.getUpdateCount()).thenReturn(-1);

        // run
        service.preparedQuery(connection, "query", Arrays.asList(parameters), 20, null);

        // verify
        verify(service, times(2)).parseNamedParameters(connection, "query");
        verify(service).fillStatement(parameters, statement, 1);
        verify(statement, times(2)).getUpdateCount();
        verify(statement).execute();
        verify(statement).setQueryTimeout(20);
    }

    /**
     * <p>
     * Tests the query method when:
     * </p>
     * <ul>
     * <li>The parameters are null</li>
     * <li>This parameter creates a result where first and second UpdateCount exist.</li>
     * </ul>
     *
     * @throws SQLException
     */
    @Test
    public void testQueryWithNullParameterAndFirstAndSecondCount() throws SQLException {
        // Mock the input
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());

        // Mock the process
        when(service.parseNamedParameters(connection, "query")).thenReturn(statement);
        when(service.extractParameterNames("query")).thenReturn(Arrays.asList());
        when(statement.getUpdateCount()).thenReturn(5);
        // run
        service.preparedQuery(connection, "query", null, 20, null);

        // verify
        verify(service, times(2)).parseNamedParameters(connection, "query");
        verify(statement, times(3)).getUpdateCount();
        verify(statement).execute();
        verify(statement).setQueryTimeout(20);
    }

    /**
     * <p>
     * Tests the query method when:
     * </p>
     * <ul>
     * <li>The parameters are null</li>
     * <li>This parameter creates a result where first and second UpdateCount exist.</li>
     * </ul>
     *
     * @throws SQLException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testQueryWithNullParameterExeception() throws SQLException {
        // Mock the input
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());

        // Mock the process
        when(service.parseNamedParameters(connection, "query")).thenReturn(statement);
        when(service.extractParameterNames("query")).thenReturn(Arrays.asList("a parameter name"));
        // run
        service.preparedQuery(connection, "query", null, 20, null);
    }

    /**
     * Test the parseNamedParameters function with normal usage
     *
     * @throws SQLException
     */
    @Test
    public void testParseNamedParameters() throws SQLException {
        // Mock
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        when(connection.prepareStatement("SELECT * FROM ?")).thenReturn(statement);

        // Run
        PreparedStatement output = service.parseNamedParameters(connection, "SELECT * FROM :ThisWillBeReplaced");

        // Assert
        Assert.assertEquals(output, statement);
    }

    /**
     * Test the extractParameterNames function and asserts whether the result is in the correct order.
     */
    @Test
    public void testExtractParameterNames() {
        // mock
        String query = ":first :second :third";
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());

        // run
        List<String> output = service.extractParameterNames(query);

        // assert
        Assert.assertEquals(output.get(0), "first");
        Assert.assertEquals(output.get(1), "second");
        Assert.assertEquals(output.get(2), "third");
    }

    /**
     * <p>
     * Test the executeBatch method with normal usage.
     * </p>
     * <p>
     * We hand as arguments a list containing two parameters with each having two fields with values
     * </p>
     * <p>
     * We verify whether we actually set an object four times and add a batch twice
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testExecuteBatch() throws SQLException {
        // mock
        BaseDatabaseService service = new BaseDatabaseServiceImpl();
        PreparedStatement statement = mock(PreparedStatement.class);

        LinkedHashMap<String, Object> parameterBatch = new LinkedHashMap<String, Object>();
        parameterBatch.put("first", "firstValue");
        parameterBatch.put("second", "secondValue");

        List<LinkedHashMap<String, Object>> input = Arrays.asList(parameterBatch, parameterBatch);
        List<String> indexedParameters = Arrays.asList("first", "second");

        // run
        service.executeBatch(statement, indexedParameters, input);

        // verify
        verify(statement, times(4)).setObject(anyInt(), any());
        verify(statement, times(2)).addBatch();
    }

    /**
     * We test whether the executeBatch method throws a correct error when it receives a parameter which does not contain a field it should contain</p>
     *
     * @throws SQLException
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "The Parameters argument should contain: \"first\"")
    public void testExecuteBatchWithInvalidParameters() throws SQLException {
        // mock
        BaseDatabaseService service = new BaseDatabaseServiceImpl();
        PreparedStatement statement = mock(PreparedStatement.class);

        LinkedHashMap<String, Object> parameterBatch = new LinkedHashMap<String, Object>();
        parameterBatch.put("notFirst", "firstValue");
        parameterBatch.put("second", "secondValue");

        List<LinkedHashMap<String, Object>> input = Arrays.asList(parameterBatch, parameterBatch);
        List<String> indexedParameters = Arrays.asList("first", "second");

        // run
        service.executeBatch(statement, indexedParameters, input);

        // verify
        verify(statement, times(4)).setObject(anyInt(), any());
        verify(statement, times(2)).addBatch();
    }

    /**
     * <p>
     * Test the JDBCURL method with the following settings:
     * </p>
     * <ul>
     * <li>name = null</li>
     * <li>pass = null</li>
     * <li>options = empty</li>
     * <li>type = "type"</li>
     * <li>database = "database"</li>
     * <li>optionsMarker = "%d"</li>
     * <li>optionsSeperator = "?"</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateJDBCURLWithNoNameNoPassNoOptions() {
        // run
        BaseDatabaseService service = new BaseDatabaseServiceImpl();
        String output = service.createJDBCURL("type", "database", null, null, "%", "?");

        // assert
        Assert.assertEquals(output, "jdbc:type://database%");
    }

    /**
     * <p>
     * Test the JDBCURL method with the following settings:
     * </p>
     * <ul>
     * <li>name = "Name"</li>
     * <li>pass = "Pass"</li>
     * <li>options = &ltoption1, firstOption&gt</li>
     * <li>type = "type"</li>
     * <li>database = "database"</li>
     * <li>optionsMarker = "%"</li>
     * <li>optionsSeperator = "?"</li>
     * </ul>
     */
    @Test
    public void testCreateJDBCURLWithNameAndPassAndOptions() {
        // run
        BaseDatabaseService service = new BaseDatabaseServiceImpl();
        @SuppressWarnings("unchecked")
        String output = service.createJDBCURL("type", "database", "Name", "Pass", "%", "?", new Tuple<String, String>("option1", "firstOption"));

        // assert
        Assert.assertEquals(output, "jdbc:type://database%user=Name?password=Pass?option1=firstOption?");
    }

    /**
     * <p>
     * Test the getObject method with normal usage.
     * </p>
     * <p>
     * we hand a hashmap of constaints, one having a not null value and one having an actual value
     * <p>
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetObject() throws SQLException, ConversionException {

        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultMetaData = mock(ResultSetMetaData.class);

        LinkedHashMap<String, Object> constraints = new LinkedHashMap<String, Object>();
        constraints.put("notnull", "value");
        constraints.put("null", null);

        // Make sure we hand the correct ResultMetaData in the process
        when(service.createSelectQuery(eq(connection), eq("table"), Matchers.anyList())).thenReturn("selectQuery");
        when(connection.prepareStatement("selectQuery")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultMetaData);
        when(resultSet.next()).thenReturn(true);
        when(resultMetaData.getColumnCount()).thenReturn(2);
        when(resultMetaData.getColumnName(anyInt())).thenReturn("name");

        // run
        Map<String, Object> output = service.getObject(connection, "table", constraints);

        // verify we adjust all constraints
        verify(resultMetaData, times(2)).getColumnName(anyInt());

        // assert
        Assert.assertEquals(output.size(), 1);
    }

    /**
     * Test whether an exception is thrown in the getObject method when no object could be found.
     *
     * @throws SQLException
     * @throws ConversionException
     */
    @SuppressWarnings("unchecked")
    public void testGetObjectWithNoObjectsFound() throws SQLException, ConversionException {
        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        LinkedHashMap<String, Object> constraints = new LinkedHashMap<String, Object>();

        // Make sure we hand the correct ResultMetaData in the process
        when(service.createSelectQuery(eq(connection), eq("`table`"), Matchers.anyList())).thenReturn("selectQuery");
        when(connection.prepareStatement("selectQuery")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        // run
        LinkedHashMap<String, Object> result = service.getObject(connection, "table", constraints);

        assertNull(result);
    }

    /**
     * Test whether the setValue method throws an exception when it fails to set a value in a column.
     *
     * @throws SQLException
     */
    @Test(expectedExceptions = SQLException.class, expectedExceptionsMessageRegExp = "Failed to set value 'value' for column 'key'.")
    public void testSetValueFailure() throws SQLException {
        BaseDatabaseService service = new BaseDatabaseServiceImpl();
        PreparedStatement statement = mock(PreparedStatement.class);

        doThrow(new RobotRuntimeException("I failed")).when(statement).setObject(anyInt(), any());

        // run
        service.setValue(statement, "key", "value", 0);
    }

    /**
     * Test the create select query method when the keys are not empty
     *
     * @throws SQLException
     */
    @Test
    public void testCreateSelectQueryWithKeys() throws SQLException {
        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        List<String> keys = Arrays.asList("key1", "key2");

        doReturn("constraintSql").when(service).createQueryPart(connection, keys, " AND ");
        doReturn("output").when(service).createSelectQuery("table", "constraintSql");

        // run
        String output = service.createSelectQuery(connection, "table", keys);

        // verify
        verify(service, times(1)).createQueryPart(connection, keys, " AND ");

        // Assert
        Assert.assertNotEquals(output, "output");
    }

    /**
     * Test the create select query method when the keys are empty
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateSelectQueryWithoutKeys() throws SQLException {
        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        List<String> keys = Arrays.asList();

        doReturn("output").when(service).createSelectQuery("table", "1");

        // run
        String output = service.createSelectQuery(connection, "table", keys);

        // verify
        verify(service, times(0)).createQueryPart(any(), anyList(), anyString());

        // Assert
        Assert.assertNotEquals(output, "output");
    }

    /**
     * Test the create query part when the keys are not empty.
     *
     * @throws SQLException
     */
    @Test
    public void testCreateQueryPart() throws SQLException {
        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        List<String> keys = Arrays.asList("key1", "key2");

        doReturn("firstKey").when(service).escapeIdentifier("key1", connection);
        doReturn("secondKey").when(service).escapeIdentifier("key2", connection);

        // run
        String output = service.createQueryPart(connection, keys, " AND ");

        // assert
        Assert.assertEquals(output, "firstKey = ? AND secondKey = ?");
    }

    /**
     * Test the escape identifier method with ? as escape symbol and "identifier" as the value we want to escape.
     *
     * @throws SQLException
     */
    @Test
    public void testEscapeIdentifier() throws SQLException {
        BaseDatabaseService service = new BaseDatabaseServiceImpl();
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);

        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getIdentifierQuoteString()).thenReturn("?");

        String output = service.escapeIdentifier("identifier", connection);

        Assert.assertEquals(output, "?identifier?");
    }

    /**
     * <p>
     * Test the storeObject method with no keys
     * </p>
     * <p>
     * It should call insertObject
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testStoreObjectWithEmptyKeys() throws SQLException {
        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        LinkedHashMap<String, Object> newObject = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        doNothing().when(service).insertObject(connection, "table", newObject);

        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        // run
        service.storeObject(connection, "table", newObject, keys, false);

        // verify
        verify(service, times(1)).storeObject(connection, "table", newObject, keys, false);
    }

    /**
     * <p>
     * Test the storeObject method with keys and overwrite set to true
     * </p>
     * <p>
     * It should call insertObject
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testStoreObjectWithOverwrite() throws SQLException, ConversionException {
        // Setup input objects
        LinkedHashMap<String, Object> newObject = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        keys.add("key");

        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        doReturn(true).when(service).checkIfExists(connection, "table", newObject, keys);

        doNothing().when(service).updateObject(connection, "table", newObject, keys);


        // run
        service.storeObject(connection, "table", newObject, keys, true);

        // verify
        verify(service, times(1)).updateObject(connection, "table", newObject, keys);
        verify(service, times(0)).insertObject(connection, "table", newObject);
    }

    /**
     * <p>
     * Test the storeObject method with keys and overwrite set to true
     * </p>
     * <p>
     * It should call insertObject
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testStoreObjectWithInsert() throws SQLException, ConversionException {
        // Setup input objects
        LinkedHashMap<String, Object> newObject = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        keys.add("key");

        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);
        doReturn(false).when(service).checkIfExists(connection, "table", newObject, keys);

        doNothing().when(service).insertObject(connection, "table", newObject);


        // run
        service.storeObject(connection, "table", newObject, keys, false);

        // verify
        verify(service, times(1)).insertObject(connection, "table", newObject);
        verify(service, times(0)).updateObject(connection, "table", newObject, keys);
    }

    /**
     * <p>
     * Test the storeObject method with keys and overwrite set to false
     * </p>
     * <p>
     * It should call updateObject
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testStoreObjectWithKeysAndNoOverwrite() throws SQLException, ConversionException {
        // Setup input objects
        LinkedHashMap<String, Object> newObject = new LinkedHashMap<>();
        newObject.put("key", "keyValue");

        List<String> keys = new ArrayList<>();
        keys.add("key");

        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());
        Connection connection = mock(Connection.class);

        doReturn(true).when(service).checkIfExists(connection, "table", newObject, keys);

        // run
        service.storeObject(connection, "table", newObject, keys, false);

        // verify object is set
        verify(service, times(0)).updateObject(connection, "table", newObject, keys);
        verify(service, times(0)).insertObject(connection, "table", newObject);
    }

    /**
     * <p>
     * Test the insertObject method
     * </p>
     *
     * @throws SQLException
     */
    @Test
    public void testInsertObject() throws SQLException {
        // mock
        BaseDatabaseService service = spy(new BaseDatabaseServiceImpl());

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        LinkedHashMap<String, Object> newObject = new LinkedHashMap<>();
        newObject.put("firstValue", "key");
        newObject.put("secondValue", "lock");

        doReturn("key").when(service).escapeIdentifier(anyString(), eq(connection));
        doNothing().when(service).fillStatement(eq(newObject), any(), eq(1));

        // run
        service.insertObject(connection, "table", newObject);

        // verify
        verify(service, times(2)).escapeIdentifier(anyString(), eq(connection));
        verify(service, times(1)).fillStatement(eq(newObject), any(), eq(1));
        verify(connection, times(1)).prepareStatement(anyString());
    }


    /**
     * Basic implementation of a BaseDatabaseService class
     */
    private class BaseDatabaseServiceImpl extends BaseDatabaseService {
        public String MOCKED_CONNECTION_URL = "MOCKED_CONNECTION";


        @SafeVarargs
        @Override
        public final Connection createConnection(String database, String user, String pass, Tuple<String, String>... options) throws SQLException {
            return mock(Connection.class);
        }

        @SafeVarargs
        @Override
        protected final String createConnectionURL(String database, String user, String pass, Tuple<String, String>... options) throws SQLException {
            return MOCKED_CONNECTION_URL;
        }

        @Override
        public void loadDriver() throws ClassNotFoundException {
        }
    }
}
