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
package nl.xillio.xill.plugins.mongodb.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.mongodb.ConnectionFailedException;
import nl.xillio.xill.plugins.mongodb.services.Connection;
import nl.xillio.xill.plugins.mongodb.services.ConnectionInfo;
import nl.xillio.xill.plugins.mongodb.services.ConnectionManager;

/**
 * This construct will connect to a MongoDB server.
 *
 * @author Thomas Biesaart
 * @author Titus Nachbauer
 */
public class ConnectConstruct extends Construct {
    private final ConnectionManager connectionManager;

    @Inject
    public ConnectConstruct(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (database, host, port, username, password) ->
                        process(database, host, port, username, password, context),
                new Argument("database", ATOMIC),
                new Argument("host", fromValue("localhost"), ATOMIC),
                new Argument("port", fromValue(27017), ATOMIC),
                new Argument("username", NULL, ATOMIC),
                new Argument("password", NULL, ATOMIC)
        );
    }

    MetaExpression process(MetaExpression databaseExpression, MetaExpression hostExpression, MetaExpression portExpression, MetaExpression usernameExpression, MetaExpression passwordExpression, ConstructContext context) {
        // Get the information
        String host = hostExpression.getStringValue();
        int port = portExpression.getNumberValue().intValue();
        String database = databaseExpression.getStringValue();
        String username = usernameExpression.isNull() ? null : usernameExpression.getStringValue();
        String password = passwordExpression.isNull() ? null : passwordExpression.getStringValue();
        ConnectionInfo info = new ConnectionInfo(host, port, database, username, password);

        // Create/Get the connection
        MetaExpression result = fromValue(info.toString());
        Connection connection = connect(context, info);
        result.storeMeta(connection);

        return result;
    }

    private Connection connect(ConstructContext context, ConnectionInfo info) {
        try {
            return connectionManager.getConnection(context, info);
        } catch (ConnectionFailedException e) {
            throw new RobotRuntimeException("Failed to connect to " + info, e);
        }
    }
}
