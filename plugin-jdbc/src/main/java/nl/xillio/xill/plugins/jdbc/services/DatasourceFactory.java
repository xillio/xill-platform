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
package nl.xillio.xill.plugins.jdbc.services;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * This factory provides an adapter between datasource factories and connection factories.
 * The implementations of this factory should build a {@link DataSource} which will be used to create connections.
 * This is the preferred way of connecting to a database
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public abstract class DatasourceFactory extends ConnectionFactory {
    @Override
    protected Connection buildConnection(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException {
        DataSource dataSource = buildSource(context, connectionString, options);
        return dataSource.getConnection();
    }

    protected abstract DataSource buildSource(ConstructContext context, String connectionString, Map<String, MetaExpression> options) throws SQLException;
}
