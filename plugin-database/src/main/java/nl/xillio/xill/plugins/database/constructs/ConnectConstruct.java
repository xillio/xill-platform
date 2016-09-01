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

import com.google.inject.Inject;
import nl.xillio.xill.api.XillThreadFactory;
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
import nl.xillio.xill.plugins.database.util.Tuple;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The construct for the connect function.
 */
@Deprecated
public class ConnectConstruct extends BaseDatabaseConstruct {

    @Inject
    public ConnectConstruct(XillThreadFactory xillThreadFactory) {
        super(xillThreadFactory);
    }

    @Override
    public ConstructProcessor doPrepareProcess(final ConstructContext context) {
        Argument[] args =
                {new Argument("database", ATOMIC),
                        new Argument("type", ATOMIC),
                        new Argument("user", NULL, ATOMIC),
                        new Argument("pass", NULL, ATOMIC),
                        new Argument("options", fromValue(new LinkedHashMap<>()), OBJECT)};
        return new ConstructProcessor(a -> process(a, factory, context, context.getRootRobot()), args);
    }

    @SuppressWarnings("unchecked")
    static MetaExpression process(final MetaExpression[] args, final DatabaseServiceFactory factory, final ConstructContext context, final RobotID robotID) {
        String database = args[0].isNull() ? null : args[0].getStringValue();
        String type = args[1].getStringValue();
        String user = args[2].isNull() ? null : args[2].getStringValue();
        String pass = args[3].isNull() ? null : args[3].getStringValue();
        Map<String, MetaExpression> options = args[4].getValue();

        return process(context, database, type, user, pass, options, factory, robotID);
    }

    @SuppressWarnings("unchecked")
    static MetaExpression process(ConstructContext context, String database, String type, String user, String pass, Map<String, MetaExpression> options, DatabaseServiceFactory factory, RobotID robotID) {

        Tuple<String, String>[] optionsArray =
                (Tuple<String, String>[]) options.entrySet().stream()
                        .map(e -> new Tuple<>(e.getKey(), e.getValue().getStringValue()))
                        .toArray(Tuple[]::new);

        DatabaseService service;
        try {
            service = factory.getService(type);
        } catch (ReflectiveOperationException | IllegalArgumentException e1) {
            throw new RobotRuntimeException("Connection Error: Database type is not supported", e1);
        }

        final Connection connection = createConnection(context, service, database, user, pass, optionsArray);

        MetaExpression metaExpression = fromValue(database);
        ConnectionMetadata newConnection = new ConnectionMetadata(type, connection);
        // add the robotId with the new connection to the pool
        setLastConnection(robotID, newConnection);
        // store the connection metadata in the output MetaExpression
        metaExpression.storeMeta(newConnection);

        return metaExpression;
    }

    private static Connection createConnection(ConstructContext context, DatabaseService service, String database, String user, String pass,
                                               Tuple<String, String>[] optionsArray) {
        try {
            Connection connection = service.createConnection(database, user, pass, optionsArray);
            //Add a listener that stops the connection once the robot is closed.
            context.addRobotStoppedListener(x -> service.closeConnection(connection));

            return connection;
        } catch (SQLException e) {
            throw new RobotRuntimeException(getExceptionMessage(e), e);
        }
    }
    
    /**
     * Hands a fitting exception message for a given Exception.
     * @param e
     *          The Exception we're parsing.
     * @return
     *          A fitting message
     */
    private static String getExceptionMessage(SQLException e)
    {
        Throwable cause = e.getCause();
        if (cause != null && cause instanceof UnknownHostException) {
            return "Connection error: Unknown database host";
        } else if (cause != null && cause instanceof ConnectException) {
            return "Connection error: Unknown database port";
        } else if (cause != null && cause instanceof IOException) {
            return "Connection error: Could not connect to database";
        } else {
            return e.getMessage();
        }
    }
}
