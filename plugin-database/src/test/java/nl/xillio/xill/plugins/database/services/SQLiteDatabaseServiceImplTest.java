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

import nl.xillio.xill.plugins.database.util.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static nl.xillio.xill.plugins.database.services.DatabaseServiceTestUtils.baseDatabaseServiceStubs;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Tests the SQLite specific database service
 *
 * @author Geert Konijnendijk
 */
public class SQLiteDatabaseServiceImplTest {

    private SQLiteDatabaseServiceImpl service;

    /**
     * Setup the service
     */
    @BeforeClass
    public void setup() {
        service = new SQLiteDatabaseServiceImpl();
    }

    /**
     * Test the createConnection method
     *
     * @throws SQLException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testCreateConnection() throws SQLException {
        // Mock
        Connection con = mock(Connection.class);
        String connectionURL = "URL";
        Tuple[] options = new Tuple[]{};
        SQLiteDatabaseServiceImpl spyService = spy(service);
        baseDatabaseServiceStubs(spyService, con, connectionURL);

        // Run
        Connection returnedCon = spyService.createConnection("db", null, null, options);

        // Verify
        verify(spyService).createConnectionURL(notNull(String.class), isNull(String.class), isNull(String.class));
        verify(spyService).connect(connectionURL);

        // Assert
        assertSame(returnedCon, con, "Different connection was returned");
    }

    /**
     * Test the creation of a URL for an in-memory database
     *
     * @throws SQLException
     */
    @Test
    public void testCreateConnectionURLMemory() throws SQLException {
        String database = null;
        testCreateConnectionURL(database, "jdbc:sqlite::memory:");
    }

    /**
     * Test the creation of a URL for a file database
     *
     * @throws SQLException
     */
    @Test
    public void testCreateConnectionURLFile() throws SQLException {
        String database = "test.sqlite";
        testCreateConnectionURL(database, "jdbc:sqlite:test.sqlite");
    }

    private void testCreateConnectionURL(String database, String expectedURL) throws SQLException {
        // Run
        @SuppressWarnings("unchecked")
        String URL = service.createConnectionURL(database, null, null);

        // Verify

        // Assert
        assertEquals(URL, expectedURL, "Incorrect URL created");
    }
}
