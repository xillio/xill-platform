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
package nl.xillio.xill.plugins.mongodb.constructs;


import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.mongodb.services.Connection;
import nl.xillio.xill.plugins.mongodb.services.ConnectionFactory;
import nl.xillio.xill.plugins.mongodb.services.ConnectionManager;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ConnectConstructTest extends TestUtils {
    @Test
    public void testCreateNewConnection() {
        ConnectionFactory factory = mock(ConnectionFactory.class, RETURNS_DEEP_STUBS);
        ConnectionManager connectionManager = new ConnectionManager(factory);
        ConnectConstruct connectConstruct = new ConnectConstruct(connectionManager);
        ConstructContext context = new ConstructContext(RobotID.dummyRobot(), RobotID.dummyRobot(), connectConstruct, null, UUID.randomUUID(), null, null);
        MetaExpression result = connectConstruct.process(
                fromValue("database"),
                fromValue("localhost"),
                fromValue(1423),
                fromValue("username"),
                fromValue("password123"),
                context);

        assertEquals(result.getStringValue(), "Mongo[username@localhost:1423/database]");
        assertFalse(result.getMeta(Connection.class).isClosed());
    }
}
