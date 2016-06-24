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
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link ConnectConstruct}
 */
public class ConnectConstructTest extends TestUtils {

    /**
     * test the method when all given input is valid. does not throw any exceptions
     *
     * @throws SQLException
     * @throws ReflectiveOperationException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testProcessNormal() throws SQLException, ReflectiveOperationException {
        // mock
        // mock all the input
        MetaExpression database = mockExpression(ATOMIC, true, 0, "databaseName");
        MetaExpression type = mockExpression(ATOMIC, true, 0, "databaseType");
        MetaExpression user = mockExpression(ATOMIC, true, 0, "user");
        MetaExpression pass = mockExpression(ATOMIC, true, 0, "pass");
        MetaExpression options = mockExpression(OBJECT);
        ConstructContext context = mock(ConstructContext.class);
        when(options.getValue()).thenReturn(new LinkedHashMap<>());

        MetaExpression[] args = {database, type, user, pass, options};
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        RobotID robotID = mock(RobotID.class);

        // mock the connection and service
        Connection connection = mock(Connection.class);
        DatabaseService dbService = mock(DatabaseService.class);

        when((dbService).createConnection(eq("databaseName"), eq("user"), eq("pass"), any())).thenReturn(connection);
        when((factory).getService("databaseType")).thenReturn(dbService);
        ConnectionMetadata metadata = mock(ConnectionMetadata.class);

        BaseDatabaseConstruct.setLastConnection(robotID, metadata);

        mock(BaseDatabaseConstruct.class);

        // run
        MetaExpression result = ConnectConstruct.process(args, factory, context, robotID);

        // verify
        verify(dbService, times(1)).createConnection(eq("databaseName"), eq("user"), eq("pass"));
        // verify(bdbc,times(1)).setLastConnections(eq(robotID), any());
        verify(factory, times(1)).getService("databaseType");

        // assert
        Assert.assertEquals(result, fromValue("databaseName"));

    }

    /**
     * This test checks whether the method throws an exception when service.createconnection goes wrong.
     *
     * @throws Throwable
     * @throws RobotRuntimeException
     */
    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "...")
    public void testProcessSQLException() throws Throwable {
        // the args
        MetaExpression database = mockExpression(ATOMIC, true, 0, "databaseName");
        MetaExpression type = mockExpression(ATOMIC, true, 0, "databaseType");
        MetaExpression user = mockExpression(ATOMIC, true, 0, "user");
        MetaExpression pass = mockExpression(ATOMIC, true, 0, "pass");
        MetaExpression options = mockExpression(OBJECT);
        ConstructContext context = mock(ConstructContext.class);
        when(options.getValue()).thenReturn(new LinkedHashMap<>());

        MetaExpression[] args = {database, type, user, pass, options};

        // the factory and its settings
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        DatabaseService dbService = mock(DatabaseService.class);
        when((factory).getService("databaseType")).thenReturn(dbService);
        when((dbService).createConnection(eq("databaseName"), eq("user"), eq("pass"))).thenThrow(new SQLException("..."));

        // the robotID
        RobotID robotID = mock(RobotID.class);

        // run
        ConnectConstruct.process(args, factory, context, robotID);
    }

    /**
     * @return Returns an array containing all exceptions we throw
     */
    @DataProvider(name = "exceptions")
    private Object[][] allExceptions() {
        return new Object[][]{
                {null, new InstantiationException()},
                {null, new IllegalAccessException()},
                {null, new ClassNotFoundException()}
        };
    }

    /**
     * This test checks whether the method throws an exception when factory.getService goes wrong.
     *
     * @param o Fodder object needed by testNG.
     * @param e The exception we throw when getting a service.
     * @throws Throwable
     * @throws RobotRuntimeException
     */
    @Test(dataProvider = "exceptions", expectedExceptions = RobotRuntimeException.class)
    public void testProcessGetServiceException(final Object o, final Exception e) throws Throwable {
        // mock

        // the arguments
        MetaExpression database = mockExpression(ATOMIC, true, 0, "databaseName");
        MetaExpression type = mockExpression(ATOMIC, true, 0, "databaseType");
        MetaExpression user = mockExpression(ATOMIC, true, 0, "user");
        MetaExpression pass = mockExpression(ATOMIC, true, 0, "pass");
        MetaExpression options = mockExpression(OBJECT);
        ConstructContext context = mock(ConstructContext.class);
        when(options.getValue()).thenReturn(new LinkedHashMap<>());

        MetaExpression[] args = {database, type, user, pass, options};

        // the factory and its settings
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        mock(Connection.class);
        mock(DatabaseService.class);
        when((factory).getService("databaseType")).thenThrow(e);

        // the robotID
        RobotID robotID = mock(RobotID.class);

        // run
        ConnectConstruct.process(args, factory, context, robotID);
    }
}
