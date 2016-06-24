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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.jdbc.services.ConnectionFactory;
import nl.xillio.xill.plugins.jdbc.services.ExpressionConverter;
import nl.xillio.xill.plugins.jdbc.services.QueryingService;
import nl.xillio.xill.plugins.jdbc.services.StatementSyntaxFactory;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;


public class GetObjectConstructTest extends TestUtils {

    @Test
    public void testGetObjectNormal() throws SQLException {
        // The input
        String tableName = "myTestingTable";
        String id = "rt9748ugyt74259ug02h7389ug42";

        LinkedHashMap<String, MetaExpression> object = new LinkedHashMap<>();
        object.put("id", fromValue(id));
        MetaExpression objectInput = fromValue(object);

        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        // The mock output
        MetaExpression output = fromValue("I AM MOCKED OUTPUT");

        // Mock all the services
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.getOrError(any(), any())).thenReturn(connection);

        StatementSyntaxFactory statementFactory = mock(StatementSyntaxFactory.class);
        when(statementFactory.selectOne(same(connection), eq(tableName), any())).thenReturn(statement);

        QueryingService queryingService = mock(QueryingService.class);
        when(queryingService.singleResult(statement)).thenReturn(output);

        ExpressionConverter expressionConverter = new ExpressionConverter();

        GetObjectConstruct construct = new GetObjectConstruct(connectionFactory, statementFactory, queryingService, expressionConverter, null);

        // Process
        MetaExpression result = process(construct, fromValue(tableName), objectInput);

        // Verify that the 'query' has been executed
        assertSame(result, output);
        verify(queryingService).singleResult(statement);

    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessSQLExceptionOnStatement() throws SQLException {
        // The input
        String tableName = "myTestingTable";

        // Mock all the services
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        when(connectionFactory.getOrError(any(), any())).thenReturn(mock(Connection.class));

        StatementSyntaxFactory statementFactory = mock(StatementSyntaxFactory.class);
        when(statementFactory.selectOne(any(), eq(tableName), any())).thenThrow(SQLException.class);

        ExpressionConverter expressionConverter = new ExpressionConverter();

        GetObjectConstruct construct = new GetObjectConstruct(connectionFactory, statementFactory, null, expressionConverter, null);

        // Process
        process(construct, fromValue(tableName), emptyObject());
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testGetObjectSQLExceptionOnQuery() throws SQLException {
        // The input
        String tableName = "myTestingTable";
        String id = "rt9748ugyt74259ug02h7389ug42";

        LinkedHashMap<String, MetaExpression> object = new LinkedHashMap<>();
        object.put("id", fromValue(id));
        MetaExpression objectInput = fromValue(object);

        // The mock output
        MetaExpression output = fromValue("I AM MOCKED OUTPUT");

        // Mock all the services
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        StatementSyntaxFactory statementFactory = mock(StatementSyntaxFactory.class);

        QueryingService queryingService = mock(QueryingService.class);
        when(queryingService.singleResult(any())).thenThrow(SQLException.class);

        ExpressionConverter expressionConverter = new ExpressionConverter();

        GetObjectConstruct construct = new GetObjectConstruct(connectionFactory, statementFactory, queryingService, expressionConverter, null);
        process(construct, fromValue(tableName), objectInput);
    }

}