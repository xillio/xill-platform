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
package nl.xillio.xill.plugins.jdbc.constructs;

import com.google.inject.name.Named;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.data.ConnectionWrapper;
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;
import org.testng.annotations.Test;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ConnectConstructTest extends TestUtils {
    @Test
    public void testConnectNormal() throws SQLException {
        Connection connection = mock(Connection.class);
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.createAndStore(any(), anyString(), any())).thenReturn(connection);
        ConnectConstruct connectConstruct = new ConnectConstruct(connectionFactory, null);

        MetaExpression result = process(connectConstruct, fromValue("jdbc:imaginay/database?parameter=value"));

        Connection resultConnect = result.getMeta(ConnectionWrapper.class).getConnection();

        assertSame(resultConnect, connection);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testConnectUnauthorized() throws SQLException {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.createAndStore(any(), anyString(), any())).thenThrow(SQLInvalidAuthorizationSpecException.class);
        ConnectConstruct connectConstruct = new ConnectConstruct(connectionFactory, null);

        process(connectConstruct, fromValue("jdbc:imaginay/database?parameter=value"));
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testConnectSQLException() throws SQLException {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.createAndStore(any(), anyString(), any())).thenThrow(SQLException.class);
        ConnectConstruct connectConstruct = new ConnectConstruct(connectionFactory, null);

        process(connectConstruct, fromValue("jdbc:imaginay/database?parameter=value"));
    }

    /**
     * Test if the default documentation will be fetched without a redirect
     */
    @Test
    public void testGetDocumentation() {
        ConnectConstruct connectConstruct = new MockConnectConstruct(null);
        URL url = connectConstruct.getDocumentationResource();

        assertNotNull(url);
        assertEquals(url, getClass().getResource("./ConnectConstructTest$MockConnectConstruct.xml"));
    }

    @Test
    public void testGetDocumentationWithOverride() {
        ConnectConstruct connectConstruct = new MockConnectConstruct("/test/");
        URL url = connectConstruct.getDocumentationResource();

        assertNotNull(url);
        assertEquals(url, getClass().getResource("/test/MockConnectConstruct.xml"));
    }

    @Test
    public void testConnectWithOptions() throws SQLException {
        ConnectionFactory factory = mock(ConnectionFactory.class);
        ConnectConstruct connectConstruct = new OptionsConstruct(factory, null);

        MetaExpression connectionString = fromValue("jdbc::connect");
        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
        map.put("option", fromValue("value"));
        MetaExpression options = fromValue(map);

        process(connectConstruct, connectionString, options);

        verify(factory).createAndStore(any(), eq(connectionString.getStringValue()), eq(map));
    }

    /**
     * This class is for testing the document redirect
     */
    private class MockConnectConstruct extends ConnectConstruct {

        public MockConnectConstruct(String docRoot) {
            super(null, docRoot);
        }
    }

    private class OptionsConstruct extends ConnectConstruct {

        public OptionsConstruct(ConnectionFactory connectionFactory, @Named("docRoot") String docRoot) {
            super(connectionFactory, docRoot);
        }

        @Override
        protected Argument[] buildArguments() {
            return new Argument[]{
                    new Argument("connectionString", ATOMIC),
                    new Argument("options", emptyObject(), OBJECT)
            };
        }
    }
}