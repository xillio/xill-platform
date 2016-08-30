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

import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.database.BaseDatabaseConstruct;
import nl.xillio.xill.plugins.database.services.DatabaseService;
import nl.xillio.xill.plugins.database.services.DatabaseServiceFactory;
import nl.xillio.xill.plugins.database.util.ConnectionMetadata;

@Deprecated
public class EscapeConstruct extends BaseDatabaseConstruct {

    @Override
    public ConstructProcessor doPrepareProcess(ConstructContext context) {
        return new ConstructProcessor((unescaped, database) -> process(unescaped, database, factory, context.getRootRobot()),
                new Argument("unescaped", ExpressionDataType.ATOMIC),
                new Argument("database", NULL, ATOMIC)
        );
    }

    private static MetaExpression process(final MetaExpression unescaped, final MetaExpression database, final DatabaseServiceFactory factory, final RobotID robotID) {
        String unescapedString = unescaped.getStringValue();

        // Get the database metadata
        ConnectionMetadata metaData;
        if (database.isNull()) {
            metaData = getLastConnection(robotID);
        } else {
            metaData = assertMeta(database, "database", ConnectionMetadata.class, "variable with a connection");
        }

        // Let the database service escape the string and return it.
        try {
            DatabaseService service = factory.getService(metaData.getDatabaseName());
            return fromValue(service.escapeString(unescapedString));
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new RobotRuntimeException("Illegal DBMS type", e);
        }
    }
}
