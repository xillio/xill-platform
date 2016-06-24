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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Test the {@link MssqlDatabaseServiceImpl}.
 * </p>
 * <p>
 * We only test the createSelectQuery method
 * </p>
 */
public class MssqlDatabaseServiceImplTest {

    /**
     * Test whether the create select query returns a currect query
     */
    @Test
    public void testCreateSelectQuery() {
        MssqlDatabaseServiceImpl service = new MssqlDatabaseServiceImpl();
        String output = service.createSelectQuery("TABLE", "rownum > 2");

        Assert.assertEquals(output, "SELECT TOP 1 * FROM TABLE WHERE rownum > 2");

    }

}
