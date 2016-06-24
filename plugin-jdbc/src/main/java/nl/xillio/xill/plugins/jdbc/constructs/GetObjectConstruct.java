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
import java.util.Map;

/**
 * This construct represents a query by example which returns a single item.
 * <p>
 * You can optionally provide a docRoot. If you do then the documentation for this construct will be fetched from
 * <code>docRoot + getClass().getSimpleName() + ".xml"</code> instead of the default documentation location.
 *
 * @author Thomas Biesaart
 * @since 1.0.0
 */
public class GetObjectConstruct extends Construct {
    private final ConnectionFactory connectionFactory;
    private final StatementSyntaxFactory statementSyntaxFactory;
    private final QueryingService queryingService;
    private final ExpressionConverter expressionConverter;
    private final String docRoot;

    @Inject
    public GetObjectConstruct(ConnectionFactory connectionFactory, StatementSyntaxFactory statementSyntaxFactory, QueryingService queryingService, ExpressionConverter expressionConverter, @Named("docRoot") String docRoot) {
        this.connectionFactory = connectionFactory;
        this.statementSyntaxFactory = statementSyntaxFactory;
        this.queryingService = queryingService;
        this.expressionConverter = expressionConverter;
        this.docRoot = docRoot;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (table, keys, database) -> process(table, keys, database, context),
                new Argument("table", ATOMIC),
                new Argument("keys", OBJECT),
                new Argument("database", NULL, ATOMIC)
        );
    }

    private MetaExpression process(MetaExpression table, MetaExpression keys, MetaExpression database, ConstructContext context) {
        assertNotNull(table, "table");
        String tableName = table.getStringValue();
        Map<String, Object> constraints = expressionConverter.parseConstraints(keys);

        Connection connection = connectionFactory.getOrError(context, database);
        try (PreparedStatement statement = statementSyntaxFactory.selectOne(connection, tableName, constraints)) {
            return queryingService.singleResult(statement);
        } catch (SQLException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
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
