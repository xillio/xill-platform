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
package nl.xillio.xill.plugins.mysql;

import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactoryImpl;

/**
 * This class overrides the default select one construction for mysql.
 *
 * @author Xavier Pardonnet
 */
class MySQLStatementSyntaxFactory extends StatementSyntaxFactoryImpl {
    @Override
    protected String selectOne(String escapedTableName, String constraints) {
        return String.format("SELECT * FROM %s WHERE %s LIMIT 1", escapedTableName, constraints);
    }

    @Override
    protected String selectOne(String escapedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", escapedTableName);
    }

    @Override
    protected String escapeIdentifier(String unescaped) {
        return String.format("`%s`", unescaped);
    }
}
