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

import nl.xillio.xill.plugins.database.services.BaseDatabaseService;
import nl.xillio.xill.plugins.database.services.MssqlDatabaseServiceImpl;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the Database enum
 *
 * @author Daan Knoope
 */
public class DatabaseTest {

    /**
     * Sets up a sample database and checks its properties
     *
     * @throws Exception
     */
    @Test
    public void testDatabase() throws Exception {
        Database db = Database.MSSQL;
        assertEquals(db.getName(), "mssql");
        assertEquals(db.getDriverClass(), "net.sourceforge.jtds.jdbc.Driver");
        assertEquals(db.getServiceClass(), MssqlDatabaseServiceImpl.class);
    }

    /**
     * Unit test to check if the correct database service is returned for each type
     *
     * @throws Exception
     */
    @Test
    public void testFindServiceClass() throws Exception {
        List<String> names = Arrays.asList("oracle", "mssql", "sqlite", "mysql");
        List<Class<? extends BaseDatabaseService>> expectedResults = Arrays.asList(Database.ORACLE.getServiceClass(), Database.MSSQL.getServiceClass(), Database.SQLITE.getServiceClass(),
                Database.MYSQL.getServiceClass());
        List<Class<? extends BaseDatabaseService>> returnedResults = names.stream().map(Database::findServiceClass).collect(Collectors.toList());
        assertEquals(returnedResults, expectedResults);
    }

    /**
     * Test the findService method when the DMBS type is not supported
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "DBMS type is not supported")
    public void testFindServiceClassUnsupportedType() throws Exception {
        Database.findServiceClass("googledatabase");
    }
}
