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
import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.data.ResultSetIterator;
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;
import nl.xillio.xill.plugins.jdbc.services.ExpressionConverter;
import nl.xillio.xill.plugins.jdbc.services.StatementFactory;
import org.slf4j.Logger;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;


/**
 * This construct will take a query and some options and run that query on the given connection.
 * If no connection was provided this construct will request the last cached connection for this execution from the
 * connection factory.
 * <p>
 * You can optionally provide a docRoot. If you do then the documentation for this construct will be fetched from
 * <code>docRoot + getClass().getSimpleName() + ".xml"</code> instead of the default documentation location.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public class QueryConstruct extends Construct {
    private static final Logger LOGGER = Log.get();
    private final ConnectionFactory connectionFactory;
    private final StatementFactory statementFactory;
    private final ExpressionConverter expressionConverter;
    private final String docRoot;

    @Inject
    public QueryConstruct(ConnectionFactory connectionFactory, StatementFactory statementFactory, ExpressionConverter expressionConverter, @Named("docRoot") String docRoot) {
        this.connectionFactory = connectionFactory;
        this.statementFactory = statementFactory;
        this.expressionConverter = expressionConverter;
        this.docRoot = docRoot;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (query, parameters, options, database) -> process(query, parameters, options, database, context),
                new Argument("query", ATOMIC),
                new Argument("parameters", emptyList(), OBJECT, LIST),
                new Argument("timeout", fromValue(30), ATOMIC),
                new Argument("database", NULL, ATOMIC));
    }

    private MetaExpression process(MetaExpression query, MetaExpression parameters, MetaExpression timeout, MetaExpression database, ConstructContext context) {
        assertNotNull(query, "query");
        Connection connection = connectionFactory.getOrError(context, database);
        PreparedStatement statement = statementFactory.build(connection, query, parameters, timeout);
        return executeWithInterrupt(statement, query, context);
    }

    /**
     * To allow interruption of statements we should subscribe to the interrupt event while running the statement.
     * As soon as processing has been finished we will remove the listener.
     *
     * @param statement the statement that should be executed.
     * @param query     the query that is being processed. This is solely used for error messages.
     * @param context   the construct context in which this execution is running.
     * @return The result of this query
     */
    private MetaExpression executeWithInterrupt(PreparedStatement statement, MetaExpression query, ConstructContext context) {
        Consumer<Object> cancel = o -> {
            try {
                statement.cancel();
            } catch (SQLException e) {
                LOGGER.error("Could not cancel statement", e);
            }
        };
        context.addRobotInterruptListener(cancel);
        try {
            return execute(statement, query);
        } finally {
            context.removeRobotInterruptListener(cancel);
        }
    }

    /**
     * Execute the statement and parseExpression the result based on the output.
     * This method will inspect the result of the statement and return an iterator if the result was a {@link ResultSet}
     * otherwise it will return the update count.
     *
     * @param statement the statement that should be executed.
     * @param query     the query which is solely for error messages.
     * @return the parsed result.
     */
    private MetaExpression execute(PreparedStatement statement, MetaExpression query) {
        try {
            if (statement.execute()) {
                // This is a result set
                ResultSet resultSet = statement.getResultSet();

                if (resultSet != null) {
                    ResultSetIterator iterator = new ResultSetIterator(expressionConverter, resultSet, statement);
                    MetaExpression result = fromValue("[SQL Result: " + query.toString() + "]");
                    result.storeMeta(iterator);
                    return result;
                }
            }

            // This is not a result set
            MetaExpression updateCount = fromValue(statement.getUpdateCount());
            statement.close();
            return updateCount;
        } catch (SQLException e) {
            throw new RobotRuntimeException("Failed to execute query: " + e.getMessage(), e);
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
