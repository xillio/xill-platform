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

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doReturn;

/**
 * Utility methods for service testing
 *
 * @author Geert Konijnendijk
 */
public class DatabaseServiceTestUtils {

    /**
     * Stubb methods from the {@link BaseDatabaseService} using Mockito
     *
     * @param spyService    Spy of the service
     * @param con           Connection to let {@link BaseDatabaseService#connect(String)} and {@link BaseDatabaseService#connect(String, java.util.Properties)} return
     * @param connectionURL URL to let {@link BaseDatabaseService#createConnectionURL(String, String, String, nl.xillio.xill.plugins.database.util.Tuple...)} return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static void baseDatabaseServiceStubs(BaseDatabaseService spyService, Connection con, String connectionURL) throws SQLException {
        doReturn(con).when(spyService).connect(any());
        doReturn(con).when(spyService).connect(any(), any());
        doReturn(connectionURL).when(spyService).createConnectionURL(notNull(String.class), any(), any());
        doReturn(connectionURL).when(spyService).createConnectionURL(notNull(String.class), any(), any(), any());
    }

}
