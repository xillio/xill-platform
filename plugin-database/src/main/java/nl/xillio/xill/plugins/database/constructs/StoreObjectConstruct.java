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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * this construct is for storing / updating a table from a database.
 *
 * @author Sander Visser
 */
@Deprecated
public class StoreObjectConstruct extends BaseDatabaseConstruct {

    @Override
    @SuppressWarnings("squid:S2095")  // Suppress "Resources should be closed": Arguments do not need to be closed here, because ConstructProcessor closes them
    public ConstructProcessor doPrepareProcess(final ConstructContext context) {
        Argument[] args =
                {
                        new Argument("table", ATOMIC),
                        new Argument("object", OBJECT),
                        new Argument("keys", emptyList(), LIST),
                        new Argument("allowUpdate", TRUE, ATOMIC),
                        new Argument("database", NULL, ATOMIC),
                };
        return new ConstructProcessor(a -> process(a, factory, context.getRootRobot()), args);
    }

    @SuppressWarnings("unchecked")
    static MetaExpression process(final MetaExpression[] args, final DatabaseServiceFactory factory, final RobotID robotID) {
        String tblName = args[0].getStringValue();
        LinkedHashMap<String, Object> newObject = (LinkedHashMap<String, Object>) extractValue(args[1]);
        ConnectionMetadata metaData;
        // if no database is given use the last made connection of this robot.
        if (args[4].valueEquals(NULL)) {
            metaData = getLastConnection(robotID);
        } else {
            metaData = assertMeta(args[4], "database", ConnectionMetadata.class, "variable with a connection"); // check whether the given MetaExpression has the right metaData.
        }
        Connection connection = metaData.getConnection();

        List<MetaExpression> keysMeta = (ArrayList<MetaExpression>) args[2].getValue();
        List<String> keys = keysMeta.stream().map(m -> m.getStringValue()).collect(Collectors.toList()); // create a list from the given keys.

        boolean allowUpdate = args[3].getBooleanValue();

        try {
            factory.getService(metaData.getDatabaseName()).storeObject(connection, tblName, newObject, keys, allowUpdate);
        } catch (ReflectiveOperationException | SQLException e) {
            throw new RobotRuntimeException("SQL Exception, " + e.getMessage(), e);
        }

        return NULL;

    }

}
