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
package nl.xillio.xill.plugins.sqlite;

import nl.xillio.xill.plugins.jdbc.JDBCXillPlugin;
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;
import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactory;

/**
 * This class represents the configuration of the SQLite construct package.
 *
 * @author Thomas Biesaart
 */
public class SQLiteXillPlugin extends JDBCXillPlugin {

    @Override
    protected Class<? extends ConnectionFactory> connectionFactory() {
        return SQLiteConnectionFactory.class;
    }

    @Override
    protected Class<? extends StatementSyntaxFactory> selectStatementFactory() {
        return SQLiteStatementSyntaxFactory.class;
    }
}
