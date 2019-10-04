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
package nl.xillio.xill.plugins.jdbc.constructs;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.data.ConnectionWrapper;
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.util.Map;

/**
 * This construct represents the generic implementation for a construct that connects to a database system.
 * To connect to a system this construct requires a connection factory that builds connections for the specific
 * database system.
 * <p>
 * Generally the only parameter of this construct is a connectionString (jdbc url). However you can easily override
 * the buildArguments() method to add more. All the arguments are forwarded to the connection factory.
 * <p>
 * The main responsibility for this construct is the handle exceptions that occur while building the connection.
 * <p>
 * You can optionally provide a docRoot. If you do then the documentation for this construct will be fetched from
 * <code>docRoot + getClass().getSimpleName() + ".xml"</code> instead of the default documentation location.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public class ConnectConstruct extends Construct {
    private final ConnectionFactory connectionFactory;
    private final String docRoot;

    @Inject
    public ConnectConstruct(ConnectionFactory connectionFactory, @Named("docRoot") String docRoot) {
        this.connectionFactory = connectionFactory;
        this.docRoot = docRoot;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                args -> process(context, args),
                buildArguments()
        );
    }

    /**
     * This method will provide an array of arguments for this construct. You can overload it to add more parameters to
     * your construct.
     *
     * @return an array containing the arguments for this construct
     */
    @SuppressWarnings("squid:S2095")
    // Suppress "Resources should be closed": Arguments do not need to be closed here, because ConstructProcessor closes them
    protected Argument[] buildArguments() {
        return new Argument[]{
                new Argument("connectionString", ATOMIC)
        };
    }

    /**
     * Extract a connection string from the inputs.
     *
     * @param arguments the inputs
     * @return the connection string
     */
    protected String getConnectionString(MetaExpression... arguments) {
        return arguments[0].getStringValue();
    }

    /**
     * Extract options from the inputs if they are available.
     *
     * @param arguments the inputs
     * @return an options map or null
     */
    protected Map<String, MetaExpression> getOptions(MetaExpression... arguments) {
        if (arguments.length > 1 && arguments[1].getType() == OBJECT) {
            return arguments[1].getValue();
        }
        return null;
    }

    private MetaExpression process(ConstructContext context, MetaExpression... arguments) {
        String connectionString = getConnectionString(arguments);
        Map<String, MetaExpression> options = getOptions(arguments);
        Connection connection = buildConnection(context, connectionString, options);
        MetaExpression result = fromValue("[SQL Connection]");
        result.storeMeta(new ConnectionWrapper(connection));
        return result;
    }

    private Connection buildConnection(ConstructContext context, String connectionString, Map<String, MetaExpression> options) {
        try {
            return connectionFactory.createAndStore(context, connectionString, options);
        } catch (SQLInvalidAuthorizationSpecException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new RobotRuntimeException("Could not connect to db: " + e.getMessage(), e);
        }
    }

    @Override
    public URL getDocumentationResource() {
        if (docRoot != null) {
            String stringUrl = docRoot + getClass().getSimpleName() + ".xml";
            URL url = getClass().getResource(stringUrl);
            if (url != null) {
                return url;
            }
        }

        return super.getDocumentationResource();
    }
}
