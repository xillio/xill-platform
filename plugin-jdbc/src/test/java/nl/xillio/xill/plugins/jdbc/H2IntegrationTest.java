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
package nl.xillio.xill.plugins.jdbc;

import nl.xillio.events.EventHost;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.jdbc.constructs.ConnectConstruct;
import nl.xillio.xill.plugins.jdbc.constructs.GetObjectConstruct;
import nl.xillio.xill.plugins.jdbc.constructs.QueryConstruct;
import nl.xillio.xill.plugins.jdbc.constructs.StoreObjectConstruct;
import nl.xillio.xill.plugins.jdbc.services.*;
import org.apache.commons.io.IOUtils;
import org.h2.Driver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * For the sake of real data operations. We provide an integration test using a SQLite in-memory database.
 *
 * @author Thomas Biesaart
 */
public class H2IntegrationTest extends TestUtils {
    private final ConnectionFactory connectionFactory = new TestConnectionFactory();
    private final ConnectConstruct connectConstruct = new ConnectConstruct(connectionFactory, null);
    private final ExpressionConverter expressionConverter = new ExpressionConverter();
    private final StatementFactory statementFactory = new StatementFactory(expressionConverter);
    private final StatementSyntaxFactory statementSyntaxFactory = new TestStatementSyntaxFactory();
    private final QueryConstruct queryConstruct = new QueryConstruct(connectionFactory, statementFactory, expressionConverter, null);
    private final QueryingService queryingService = new QueryingService(expressionConverter);
    private final GetObjectConstruct getObjectConstruct = new GetObjectConstruct(connectionFactory, statementSyntaxFactory, queryingService, expressionConverter, null);
    private final StoreObjectConstruct storeObjectConstruct = new StoreObjectConstruct(connectionFactory, expressionConverter, statementSyntaxFactory, queryingService, null);
    private final UUID executionId = UUID.randomUUID();
    private final Debugger debugger = new NullDebugger();

    @BeforeClass
    public void connectToDatabase() {
        run(connectConstruct, fromValue("jdbc:h2:mem:"));
    }

    @Test
    public void createTable() {
        MetaExpression result = run(queryConstruct, fromValue("CREATE TABLE test (num INT(10), value TEXT, binary BLOB)"));
        assertEquals(result.getNumberValue().intValue(), 0);
    }

    /**
     * This will run a query call with list parameters
     */
    @Test(dependsOnMethods = "createTable")
    public void insertNumRow() {
        MetaExpression result = run(queryConstruct, fromValue("INSERT INTO test (num) VALUES (?)"), fromValue(Collections.singletonList(fromValue(1337))));
        assertEquals(result.getNumberValue().intValue(), 1);
    }

    /**
     * This will use storeObject to insert a row
     */
    @Test(dependsOnMethods = "createTable")
    public void insertTextUsingStoreObject() {
        LinkedHashMap<String, MetaExpression> data = new LinkedHashMap<>();
        data.put("value", fromValue("This is textual content"));
        MetaExpression object = fromValue(data);

        MetaExpression result = run(storeObjectConstruct, fromValue("test"), object, fromValue(Collections.singletonList(fromValue("value"))));
        assertEquals(result.getBooleanValue(), true);
    }

    @Test(dependsOnMethods = "insertTextUsingStoreObject")
    public void updateTextOfItemInDatabase() {
        LinkedHashMap<String, MetaExpression> data = new LinkedHashMap<>();
        data.put("value", fromValue("This is textual content again"));
        data.put("NUM", fromValue(1337));
        MetaExpression object = fromValue(data);

        MetaExpression result = run(storeObjectConstruct, fromValue("test"), object, fromValue(Collections.singletonList(fromValue("NUM"))));
        assertEquals(result.getBooleanValue(), true);
    }

    /**
     * This will insert a row using the object notation
     */
    @Test(dependsOnMethods = "createTable")
    public void insertStreamUsingObject() {
        // Build the parameter object
        MetaExpression stream = fromValue(new SimpleIOStream(IOUtils.toInputStream("Hello World"), getClass().getSimpleName()));

        LinkedHashMap<String, MetaExpression> parameterData = new LinkedHashMap<>();
        parameterData.put("data", stream);

        MetaExpression parameters = fromValue(parameterData);

        // Run the query
        MetaExpression result = run(queryConstruct, fromValue("INSERT INTO test (binary) VALUES (:data)"), parameters);

        assertEquals(result.getNumberValue().intValue(), 1);
    }

    @Test(dependsOnMethods = "insertNumRow")
    public void getObjectExists() {
        LinkedHashMap<String, MetaExpression> example = new LinkedHashMap<>();
        example.put("num", fromValue(1337));

        MetaExpression result = run(getObjectConstruct, fromValue("test"), fromValue(example));

        assertEquals(result.toString(), "{\"NUM\":1337,\"VALUE\":null,\"BINARY\":null}");
    }

    @Test(dependsOnMethods = "createTable")
    public void getObjectDoesntExist() {
        LinkedHashMap<String, MetaExpression> example = new LinkedHashMap<>();
        example.put("num", fromValue(42));

        MetaExpression result = run(getObjectConstruct, fromValue("test"), fromValue(example));

        assertEquals(result, NULL);
    }

    /**
     * This method will fetch all the inserted data and check it.
     */
    @Test(dependsOnMethods = {"updateTextOfItemInDatabase", "insertNumRow"})
    public void getData() throws IOException {
        boolean foundNumber = false;
        boolean foundText = false;
        boolean foundBlob = false;

        MetaExpression result = run(queryConstruct, fromValue("SELECT * FROM test"));

        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);

        int count = 0;
        while (iterator.hasNext()) {
            MetaExpression row = iterator.next();
            Map<String, MetaExpression> data = row.getValue();

            MetaExpression num = data.get("NUM");
            MetaExpression text = data.get("VALUE");
            MetaExpression blob = data.get("BINARY");

            if (num != NULL) {
                assertEquals(num.getNumberValue().intValue(), 1337);
                foundNumber = true;

                // The num entry also has a text entry
                InputStream stream = text.getBinaryValue().getInputStream();
                assertEquals(IOUtils.toString(stream), "This is textual content again");
            } else if (text != NULL) {
                InputStream stream = text.getBinaryValue().getInputStream();

                assertEquals(IOUtils.toString(stream), "This is textual content");
                foundText = true;

            }

            if (blob != NULL) {
                InputStream stream = blob.getBinaryValue().getInputStream();
                assertEquals(IOUtils.toString(stream), "Hello World");
                foundBlob = true;
            }
            count++;
        }

        assertEquals(count, 3);
        assertTrue(foundNumber);
        assertTrue(foundText);
        assertTrue(foundBlob);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*Table.*")
    public void testInsertNonExistingDatabase() {
        run(queryConstruct, fromValue("INSERT INTO noExist (num) VALUES (5)"));
    }

    private class TestConnectionFactory extends ConnectionStringFactory {

        @Override
        protected Class<Driver> driver() {
            return Driver.class;
        }
    }

    private MetaExpression run(Construct construct, MetaExpression... arguments) {
        ConstructContext context = new ConstructContext(
                Paths.get("."),
                RobotID.dummyRobot(),
                RobotID.dummyRobot(),
                construct,
                debugger,
                executionId,
                new EventHost<>(),
                new EventHost<>(),
                null
        );

        return ConstructProcessor.process(
                construct.prepareProcess(context),
                arguments
        );
    }

    private class TestStatementSyntaxFactory extends StatementSyntaxFactoryImpl {

        @Override
        protected String selectOne(String escapedTableName, String constraints) {
            return String.format("SELECT * FROM %s WHERE %s LIMIT 1", escapedTableName, constraints);
        }

        @Override
        protected String selectOne(String escapedTableName) {
            return String.format("SELECT * FROM %s LIMIT 1", escapedTableName);
        }

        @Override
        protected String escapeIdentifier(String unescaped) {
            return unescaped;
        }
    }
}
