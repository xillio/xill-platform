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

import nl.xillio.events.EventHost;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.events.RobotStoppedAction;
import nl.xillio.xill.plugins.jdbc.data.ConnectionWrapper;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertSame;


public class ConnectionFactoryTest extends TestUtils {
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testCacheConnectionError() {
        ConnectionFactory connectionFactory = new MockFactory();
        connectionFactory.getOrError(
                new ConstructContext(
                        null,
                        null,
                        null,
                        null,
                        new NullDebugger(),
                        UUID.randomUUID(),
                        null,
                        null,
                        null
                ),
                NULL
        );
    }

    @Test
    public void testConnectOverride() throws SQLException {
        Connection connection = mock(Connection.class);
        MetaExpression override = fromValue("OVERRIDE");
        override.storeMeta(new ConnectionWrapper(connection));
        Debugger debugger = new NullDebugger();
        EventHost<RobotStoppedAction> robotStoppedEvent = new EventHost<>();

        ConstructContext context = new ConstructContext(
                null,
                null,
                null,
                null,
                debugger,
                UUID.randomUUID(),
                null,
                robotStoppedEvent,
                null
        );

        ConnectionFactory connectionFactory = new MockFactory();
        // Fill the cache with 1 connection
        connectionFactory.createAndStore(context, "STRING", new HashMap<>());

        // Fetch with override
        Connection connectionResult = connectionFactory.getOrError(context, override);
        assertSame(connectionResult, connection);

        // Close connection on robot stop
        robotStoppedEvent.invoke(new RobotStoppedAction(null, null));
        verify(connectionFactory.getOrError(context, null)).close();
    }

    private class MockFactory extends ConnectionFactory {

        @Override
        protected Connection buildConnection(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException {
            return mock(Connection.class);
        }
    }
}