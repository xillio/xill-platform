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
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;
import nl.xillio.xill.plugins.jdbc.services.ExpressionConverter;
import nl.xillio.xill.plugins.jdbc.services.QueryingService;
import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This construct will store or update an item in the database.
 * <p>
 * You can optionally provide a docRoot. If you do then the documentation for this construct will be fetched from
 * <code>docRoot + getClass().getSimpleName() + ".xml"</code> instead of the default documentation location.
 *
 * @author Thomas Biesaart
 */
public class StoreObjectConstruct extends Construct {
    private final ConnectionFactory connectionFactory;
    private final ExpressionConverter expressionConverter;
    private final StatementSyntaxFactory statementFactory;
    private final QueryingService queryingService;
    private final String docRoot;

    @Inject
    public StoreObjectConstruct(ConnectionFactory connectionFactory, ExpressionConverter expressionConverter, StatementSyntaxFactory statementFactory, QueryingService queryingService, @Named("docRoot") String docRoot) {
        this.connectionFactory = connectionFactory;
        this.expressionConverter = expressionConverter;
        this.statementFactory = statementFactory;
        this.queryingService = queryingService;
        this.docRoot = docRoot;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (table, object, keys, allowUpdate, database) -> process(table, object, keys, allowUpdate, database, context),
                new Argument("table", ATOMIC),
                new Argument("object", OBJECT),
                new Argument("keys", emptyList(), LIST),
                new Argument("allowUpdate", TRUE, ATOMIC),
                new Argument("database", NULL, ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression table, MetaExpression object, MetaExpression keys, MetaExpression allowUpdate, MetaExpression database, ConstructContext context) {
        assertNotNull(table, "table");
        assertNotNull(object, "object");
        assertNotNull(keys, "keys");

        Map<String, Object> data = expressionConverter.extractFromObject(object);
        if (data.isEmpty()) {
            throw new RobotRuntimeException("The provided object contained no fields. Cannot execute this query");
        }

        Connection connection = connectionFactory.getOrError(context, database);
        Map<String, Object> constraints = getConstraints(object, keys);
        String tableName = table.getStringValue();

        if (constraints.isEmpty() || !exists(connection, tableName, constraints)) {
            // The object does not exist in the database
            boolean result = insert(connection, tableName, data);
            return fromValue(result);
        } else {
            // The object exists in the database so we want to perform an update
            if (!allowUpdate.getBooleanValue()) {
                throw new RobotRuntimeException("This object already exists in the database. If you want to update it, set the `allowUpdate` parameter to true.\nObject Keys: " + constraints);
            }
            boolean result = update(connection, tableName, data, constraints);
            return fromValue(result);
        }
    }

    private boolean update(Connection connection, String tableName, Map<String, Object> data, Map<String, Object> constraints) {
        return execute(() -> statementFactory.update(connection, tableName, data, constraints));
    }

    private boolean insert(Connection connection, String tableName, Map<String, Object> data) {
        return execute(() -> statementFactory.insert(connection, tableName, data));
    }

    public boolean execute(StatementProvider provider) {
        try (PreparedStatement statement = provider.get()) {
            statement.execute();
            int result = statement.getUpdateCount();
            return result > 0;
        } catch (SQLException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }

    private Map<String, Object> getConstraints(MetaExpression object, MetaExpression keys) {
        Map<String, Object> result = new HashMap<>();
        List<MetaExpression> keyValues = keys.getValue();
        Map<String, MetaExpression> objectMap = object.getValue();

        for (MetaExpression key : keyValues) {
            String keyName = key.getStringValue();
            MetaExpression metaValue = objectMap.get(keyName);
            if (metaValue != null) {
                result.put(keyName, expressionConverter.extract(metaValue));
            }
        }

        return result;
    }

    private boolean exists(Connection connection, String tableName, Map<String, Object> constraints) {
        try (PreparedStatement statement = statementFactory.selectOne(connection, tableName, constraints)) {
            return queryingService.hasResult(statement);
        } catch (SQLException e) {
            throw new RobotRuntimeException("Could not check if object exists: " + e.getMessage(), e);
        }
    }

    private interface StatementProvider {
        PreparedStatement get() throws SQLException;
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
