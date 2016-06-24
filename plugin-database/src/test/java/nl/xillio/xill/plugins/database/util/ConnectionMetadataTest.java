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

import org.testng.annotations.Test;

import java.sql.Connection;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * Test the getters and setters of the {@link ConnectionMetadata}
 */
public class ConnectionMetadataTest {

    /**
     * Test the getters and setters.
     *
     * @throws Exception
     */
    @Test
    public void testConnectionMetaData() throws Exception {
        Connection connection = mock(Connection.class);
        Connection connection2 = mock(Connection.class);
        String name = "databasename";

        // set and get values.
        ConnectionMetadata metadata = new ConnectionMetadata(name, connection);

        assertEquals(metadata.getConnection(), connection);
        assertEquals(metadata.getDatabaseName(), name);

        // we set and get again.
        metadata.setConnection(connection2);
        assertEquals(metadata.getConnection(), connection2);
    }
}
