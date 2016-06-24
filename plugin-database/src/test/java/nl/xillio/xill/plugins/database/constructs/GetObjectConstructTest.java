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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.BaseDatabaseConstruct;
import nl.xillio.xill.plugins.database.services.DatabaseService;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import nl.xillio.xill.plugins.database.util.ConnectionMetadata;
import nl.xillio.xill.plugins.database.util.TypeConverter.ConversionException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link GetObjectConstruct}.
 */
public class GetObjectConstructTest extends TestUtils {

    /**
     * test the method with normal input, with no database given.
     * Should never use database.getMeta because lastConnections.get is called.
     *
     * @throws ReflectiveOperationException
     * @throws SQLException
     * @throws ConversionException
     */
    @Test
    public void testProcessDatabaseNull() throws ReflectiveOperationException, ConversionException, SQLException {
        MetaExpression table = mockExpression(ATOMIC, true, 0, "table");
        MetaExpression object = fromValue(new LinkedHashMap<>());
        MetaExpression database = mockExpression(ATOMIC, true, 0, "database");
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        RobotID id = mock(RobotID.class);

        ConnectionMetadata conMetadata = mock(ConnectionMetadata.class);
        Connection connection = mock(Connection.class);
        when(connection.isClosed()).thenReturn(false);
        when(conMetadata.getConnection()).thenReturn(connection);

        // so we get the lastConnections
        when(database.valueEquals(NULL)).thenReturn(true);
        BaseDatabaseConstruct.setLastConnection(id, conMetadata);

        DatabaseService dbService = mock(DatabaseService.class);
        when((factory).getService(any())).thenReturn(dbService);

        when((conMetadata).getDatabaseName()).thenReturn("databaseName");

        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
        when((dbService).getObject(any(), eq("table"), any())).thenReturn(resultMap);
        mock(BaseDatabaseConstruct.class);

        MetaExpression result = GetObjectConstruct.process(table, object, database, factory, id);

        // verify
        verify(dbService, times(1)).getObject(any(), any(), any());
        verify(database, never()).getMeta(ConnectionMetadata.class);

        Assert.assertEquals(result, emptyObject());
    }

    /**
     * test the method database.getMetadata should be called once.
     *
     * @throws ReflectiveOperationException
     * @throws SQLException
     * @throws ConversionException
     */
    @Test
    public void testProcessDatabaseNotNull() throws ReflectiveOperationException, SQLException, ConversionException {
        MetaExpression table = mockExpression(ATOMIC, true, 0, "table");
        MetaExpression object = fromValue(new LinkedHashMap<>());
        MetaExpression database = mockExpression(ATOMIC, true, 0, "database");
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        RobotID id = mock(RobotID.class);

        ConnectionMetadata conMetadata = mock(ConnectionMetadata.class);

        // so we get the lastConnections
        when(database.valueEquals(NULL)).thenReturn(false);
        mock(LinkedHashMap.class);
        BaseDatabaseConstruct.setLastConnection(id, conMetadata);

        DatabaseService dbService = mock(DatabaseService.class);
        when((factory).getService(any())).thenReturn(dbService);

        when((conMetadata).getDatabaseName()).thenReturn("databaseName");

        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
        when((dbService).getObject(any(), eq("table"), any())).thenReturn(resultMap);
        mock(BaseDatabaseConstruct.class);
        when((database).getMeta(ConnectionMetadata.class)).thenReturn(conMetadata);

        // run
        MetaExpression result = GetObjectConstruct.process(table, object, database, factory, id);

        // verify
        verify(conMetadata, times(1)).getConnection();
        verify(dbService, times(1)).getObject(any(), any(), any());
        verify(database, times(1)).getMeta(ConnectionMetadata.class);

        Assert.assertEquals(result, emptyObject());
    }

    @DataProvider(name = "exceptions")
    private Object[][] allExceptions() {
        return new Object[][]{
                {null, new InstantiationException()},
                {null, new IllegalAccessException()},
                {null, new ClassNotFoundException()}
        };
    }

    /**
     * Should throw RobotRuntimeExceptions when InstantiationException/IllegalAccesException or ClassnotFoundException occurs in getService.
     *
     * @param o Fodder object needed for testNG.
     * @param e The exception the getService method throws.
     * @throws ReflectiveOperationException
     * @throws SQLException
     * @throws ConversionException
     */
    @Test(dataProvider = "exceptions", expectedExceptions = RobotRuntimeException.class)
    public void testProcessGetServiceExceptions(final Object o, final Exception e) throws ReflectiveOperationException, SQLException, ConversionException {
        MetaExpression table = mockExpression(ATOMIC, true, 0, "table");
        MetaExpression object = fromValue(new LinkedHashMap<>());
        MetaExpression database = mockExpression(ATOMIC, true, 0, "database");
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        RobotID id = mock(RobotID.class);
        ConnectionMetadata conMetadata = mock(ConnectionMetadata.class);
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>();
        DatabaseService dbService = mock(DatabaseService.class);

        BaseDatabaseConstruct.setLastConnection(id, conMetadata);

        when(database.valueEquals(NULL)).thenReturn(false);
        when((factory).getService(any())).thenThrow(e);
        when((conMetadata).getDatabaseName()).thenReturn("databaseName");
        when((dbService).getObject(any(), eq("table"), any())).thenReturn(resultMap);
        when((database).getMeta(ConnectionMetadata.class)).thenReturn(conMetadata);

        // run
        MetaExpression result = GetObjectConstruct.process(table, object, database, factory, id);

        // verify
        verify(conMetadata, times(1)).getConnection();
        verify(dbService, times(1)).getObject(any(), any(), any());
        verify(database, times(1)).getMeta(ConnectionMetadata.class);

        Assert.assertEquals(result, NULL);
    }

    /**
     * This method should throw an robotrunTimeException when an SQLException occurs in service.getObject
     *
     * @throws ReflectiveOperationException
     * @throws SQLException
     * @throws ConversionException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessSQLException() throws ReflectiveOperationException, SQLException, ConversionException {
        MetaExpression table = mockExpression(ATOMIC, true, 0, "table");
        MetaExpression object = fromValue(new LinkedHashMap<>());
        MetaExpression database = mockExpression(ATOMIC, true, 0, "database");
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        RobotID id = mock(RobotID.class);
        ConnectionMetadata conMetadata = mock(ConnectionMetadata.class);
        mock(LinkedHashMap.class);
        DatabaseService dbService = mock(DatabaseService.class);

        BaseDatabaseConstruct.setLastConnection(id, conMetadata);

        when(database.valueEquals(NULL)).thenReturn(false);
        when((factory).getService(any())).thenReturn(dbService);
        when((conMetadata).getDatabaseName()).thenReturn("databaseName");
        when((dbService).getObject(any(), eq("table"), any())).thenThrow(new SQLException());
        when((database).getMeta(ConnectionMetadata.class)).thenReturn(conMetadata);

        // run
        MetaExpression result = GetObjectConstruct.process(table, object, database, factory, id);

        // verify
        verify(conMetadata, times(1)).getConnection();
        verify(dbService, times(1)).getObject(any(), any(), any());
        verify(database, times(1)).getMeta(ConnectionMetadata.class);

        Assert.assertEquals(result, NULL);
    }
}
