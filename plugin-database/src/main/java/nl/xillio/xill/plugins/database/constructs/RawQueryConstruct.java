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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.BaseDatabaseConstruct;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import nl.xillio.xill.plugins.database.util.ConnectionMetadata;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Construct for querying a database. Supports both reading and writing queries. Has support for multiple queries in one SQL String if the
 * underlying database driver supports it.
 *
 * @author Geert Konijnendijk
 * @author Sander Visser
 */
public class RawQueryConstruct extends BaseDatabaseConstruct {

    @Override
    public ConstructProcessor doPrepareProcess(final ConstructContext context) {
        return new ConstructProcessor((query, timeout, database) -> process(query, database, timeout, factory, context.getRootRobot()),
                new Argument("query", ATOMIC),
                new Argument("timeout", fromValue(30), ATOMIC),
                new Argument("database", NULL, ATOMIC));
    }

    static MetaExpression process(final MetaExpression query, final MetaExpression database, final MetaExpression timeout, final DatabaseServiceFactory factory, final RobotID robotID) {
        String sql = query.getStringValue();
        ConnectionMetadata metaData;

        if (database.isNull()) {
            metaData = getLastConnection(robotID);
        } else {
            metaData = assertMeta(database, "database", ConnectionMetadata.class, "variable with a connection");
        }
        Connection connection = metaData.getConnection();

        int timeoutValue = timeout.getNumberValue().intValue();

        Object result;
        try {
            result = factory.getService(metaData.getDatabaseName()).query(connection, sql, timeoutValue);

            return QueryConstruct.returnValue(result, sql);
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new RobotRuntimeException("Illegal DBMS type", e);
        } catch (SQLException | IllegalArgumentException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }
}