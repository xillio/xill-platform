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
package nl.xillio.xill.plugins.database.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.database.services.DatabaseService;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link OracleConnectConstruct}.
 */
public class OracleConnectConstructTest extends TestUtils {
    /**
     * <p>
     * Test the process's normal usage whilst using SID.
     * </p>
     * <p>
     * Note that we also run connect
     * </p>
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void testProcessNormalUsageUseSID() throws ReflectiveOperationException {
        // mock
    	ConstructContext context = mock(ConstructContext.class);

        // the factory
        DatabaseService service = mock(DatabaseService.class);
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        when(factory.getService(anyString())).thenReturn(service);

        MetaExpression options = mockExpression(ATOMIC);
        when(options.getValue()).thenReturn(new LinkedHashMap<>());

        MetaExpression[] args = {
                mockExpression(ATOMIC, false, 0, "database"),
                mockExpression(ATOMIC, false, 0, "host"),
                mockExpression(ATOMIC, false, 1531, "port"),
                mockExpression(ATOMIC, true, 0, "useSID"),
                mockExpression(ATOMIC, false, 0, "user"),
                mockExpression(ATOMIC, false, 0, "pass"),
                options
        };

        // run
        MetaExpression output = OracleConnectConstruct.process(args, factory, context);

        // assert
        Assert.assertEquals(output.getStringValue(), "host:1531:database");
    }

    /**
     * <p>
     * Test the process's normal usage when not using SID.
     * </p>
     * <p>
     * Note that we also run connect
     * </p>
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void testProcessNormalUsageNoSID() throws ReflectiveOperationException {
        // mock
    	ConstructContext context = mock(ConstructContext.class);

        // the factory
        DatabaseService service = mock(DatabaseService.class);
        DatabaseServiceFactory factory = mock(DatabaseServiceFactory.class);
        when(factory.getService(anyString())).thenReturn(service);

        MetaExpression options = mockExpression(ATOMIC);
        when(options.getValue()).thenReturn(new LinkedHashMap<>());

        MetaExpression[] args = {
                mockExpression(ATOMIC, false, 0, "database"),
                mockExpression(ATOMIC, false, 0, "host"),
                mockExpression(ATOMIC, false, 1531, "port"),
                mockExpression(ATOMIC, false, 0, "useSID"),
                mockExpression(ATOMIC, false, 0, "user"),
                mockExpression(ATOMIC, false, 0, "pass"),
                options
        };

        // run
        MetaExpression output = OracleConnectConstruct.process(args, factory, context);

        // assert
        Assert.assertEquals(output.getStringValue(), "host:1531/database");
    }
}
