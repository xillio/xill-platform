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
package nl.xillio.xill.plugins.oracle;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.jdbc.services.DatasourceFactory;
import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * This class is responsible for building {@link OracleDataSource}.
 * This class will only read the connection string and set it on the datasource.
 *
 * @author Thomas Biesaart
 */
class OracleDatasourceFactory extends DatasourceFactory {
    @Override
    protected DataSource buildSource(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException {
        OracleDataSource result = new OracleDataSource();
        result.setURL(connectionString);
        return result;
    }
}
