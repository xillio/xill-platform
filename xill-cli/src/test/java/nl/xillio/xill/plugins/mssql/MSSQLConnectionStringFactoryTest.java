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
package nl.xillio.xill.plugins.mssql;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class MSSQLConnectionStringFactoryTest {
    private final MSSQLConnectionStringFactory connectionFactory = new MSSQLConnectionStringFactory();

    @Test
    public void parseJTDSConnectionString() {
        String input = "jdbc:jtds:sqlserver://localhost/Finance;instance=sqlexpress;user=MyUserName";
        String output = connectionFactory.parseConnectionString(input);
        assertEquals(output, "jdbc:sqlserver://localhost;database=Finance;instance=sqlexpress;user=MyUserName");
    }

    @Test
    public void parseMicrosoftConnectionString() {
        String input = "jdbc:sqlserver://localhost;database=Database;instance=sqlexpress;user=MyUserName";
        String output = connectionFactory.parseConnectionString(input);
        assertEquals(output, "jdbc:sqlserver://localhost;database=Database;instance=sqlexpress;user=MyUserName");
    }
}