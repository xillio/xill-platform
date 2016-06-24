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
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.mongodb.NoSuchConnectionException;
import nl.xillio.xill.plugins.mongodb.services.Connection;
import nl.xillio.xill.plugins.mongodb.services.ConnectionManager;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * What we can test in this unit test is the management of the arguments before the actual construct logic is executed.
 * The rest is for integration tests.
 *
 * @Author Andrea Parrilli
 */
public class AbstractMongoApiConstructTest extends TestUtils {

    @Test
    public void testAbstractMongoConstructWithoutConnection() throws NoSuchConnectionException {
        Connection connection = mock(Connection.class);

        ConstructContext context = mock(ConstructContext.class);
        ConnectionManager connectionManager = mock(ConnectionManager.class, RETURNS_DEEP_STUBS);
        when(connectionManager.getConnection(context)).thenReturn(connection);

        MongoConstructForTest testConstruct = new MongoConstructForTest(connection);
        testConstruct.setConnectionManager(connectionManager);
        ConstructProcessor processor = testConstruct.prepareProcess(context);
        ConstructProcessor.process(processor, fromValue("a value"), NULL);
    }

    @Test
    public void testAbstractMongoConstructWithConnection() throws NoSuchConnectionException {
        Connection connection = mock(Connection.class);
        ConstructContext context = mock(ConstructContext.class);

        MetaExpression connectionMetaExpression = fromValue("connection for test");
        connectionMetaExpression.storeMeta(connection);

        MongoConstructForTest testConstruct = new MongoConstructForTest(connection);

        ConstructProcessor processor = testConstruct.prepareProcess(context);
        ConstructProcessor.process(processor, fromValue("a value"), connectionMetaExpression);
    }

    static class MongoConstructForTest extends AbstractMongoApiConstruct {
        private Connection expectedConnection;

        MongoConstructForTest(Connection expectedConnection) {
            this.expectedConnection = expectedConnection;
        }

        @Override
        Argument[] getApiArguments() {
            return new Argument[]{new Argument("ONE_ARGUMENT")};
        }

        @Override
        MetaExpression process(MetaExpression[] arguments, Connection connection, ConstructContext context) {
            // check we received the right arguments
            Argument[] expectedApiArgs = getApiArguments();

            assertEquals(connection, expectedConnection);
            assertEquals(arguments.length, expectedApiArgs.length);

            return NULL;
        }

        @Override
        public String getName() {
            return "MongoConstructForTest";
        }
    }
}
