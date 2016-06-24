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
import oracle.jdbc.driver.OracleConnection;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static nl.xillio.xill.plugins.database.services.DatabaseServiceTestUtils.baseDatabaseServiceStubs;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Tests the Oracle specific database service
 *
 * @author Geert Konijnendijk
 */
public class OracleDatabaseServiceImplTest {

    private OracleDatabaseServiceImpl service;

    /**
     * Sets up the service.
     */
    @BeforeClass
    public void setup() {
        service = new OracleDatabaseServiceImpl();
    }

    /**
     * Test the createConnection method
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateConnection() throws SQLException {
        // Mock
        Connection con = mock(OracleConnection.class);
        String connectionURL = "URL";
        Properties properties = new Properties();
        String user = "user", pass = "pass";
        Tuple<String, String>[] options = new Tuple[]{new Tuple<String, String>("key", "value")};
        OracleDatabaseServiceImpl spyService = spy(service);
        baseDatabaseServiceStubs(spyService, con, connectionURL);
        doReturn(properties).when(spyService).createProperties(any());

        // Run
        Connection returnedCon = spyService.createConnection("db", user, pass, options);

        // Verify
        verify(spyService).createConnectionURL(notNull(String.class), eq(user), eq(pass), eq(options[0]));
        verify(spyService).createProperties(options);
        verify(spyService).connect(connectionURL, properties);

        // Assert
        assertSame(returnedCon, con, "Different connection was returned");
    }

    /**
     * Test the createConnectionURL method with a password but without a user
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "User and pass should be both null or both non-null")
    public void testCreateConnectionURLHalfLogin() throws SQLException {
        // Mock

        // Run
        service.createConnectionURL("db", null, "pass");

        // Verify

        // Assert
    }

    /**
     * Test the createConnectionURL method with login
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateConnectionURLLogin() throws SQLException {
        // Mock

        // Run
        String URL = service.createConnectionURL("db", "user", "pass");

        // Verify

        // Assert
        assertEquals(URL, "jdbc:oracle:thin:user/pass@db", "Incorrect URL created");
    }

    /**
     * Test the createConnectionURL method without login
     *
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateConnectionURLNoLogin() throws SQLException {
        // Mock

        // Run
        String URL = service.createConnectionURL("db", null, null);

        // Verify

        // Assert
        assertEquals(URL, "jdbc:oracle:thin:@db", "Incorrect URL created");
    }

    /**
     * <p>
     * Test the create properties method.
     * </p>
     * <p>
     * We check whether the method runs correctly on normal input and verify it overwrites properties.
     * </p>
     */
    @Test
    public void testCreateProperties() {
        // Mock
        List<Tuple<String, String>> options = Arrays.asList(new Tuple<String, String>("name", "a name"),
                new Tuple<String, String>("tag", "a tag"),
                new Tuple<String, String>("name", "another name"));

        // Run
        @SuppressWarnings("unchecked")
        Properties properties = service.createProperties((Tuple<String, String>[]) options.toArray());
        // verify

        // Assert
        Assert.assertEquals(properties.getProperty("name"), "another name");
        Assert.assertEquals(properties.get("tag"), "a tag");
    }

    /**
     * Test creating a select query
     */
    @Test
    public void testCreateSelectQuery() {

        // Run
        String query = service.createSelectQuery("TABLE", "colnum > 3");

        // Assert
        Assert.assertEquals(query, "SELECT * FROM TABLE WHERE colnum > 3 AND rownum <= 1");
    }
}
