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
package nl.xillio.xill.components.operators;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.components.instructions.VariableDeclaration;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class AssignTest extends TestUtils {
    private final Debugger debugger = new NullDebugger();

    @Test
    public void testAssignToAtomic() {
        VariableDeclaration variableDeclaration = new VariableDeclaration(fromValue("Hello"), "testVar");
        variableDeclaration.process(debugger);

        Assign assign = new Assign(variableDeclaration, Collections.emptyList(), fromValue("World"));

        assertEquals(variableDeclaration.getVariable().getStringValue(), "Hello");

        assign.process(debugger);
        assertEquals(variableDeclaration.getVariable().getStringValue(), "World");
    }

    @Test
    public void testAssignToList() {
        VariableDeclaration variableDeclaration = new VariableDeclaration(
                list(fromValue("Hello")),
                "testVar"
        );
        variableDeclaration.process(debugger);


        Assign assign = new Assign(variableDeclaration, Collections.singletonList(fromValue(1)), fromValue("World"));
        assertEquals(variableDeclaration.getVariable().getStringValue(), "[\"Hello\"]");

        assign.process(debugger);
        assertEquals(variableDeclaration.getVariable().getStringValue(), "[\"Hello\",\"World\"]");
    }

    @Test
    public void testComplexAssign() {
        MetaExpression value = list(
                map(
                        "test",
                        list(
                                map(
                                        "other",
                                        fromValue(4)
                                )
                        )
                )
        );

        VariableDeclaration variableDeclaration = new VariableDeclaration(value, "test");
        variableDeclaration.process(debugger);
        Assign assign = new Assign(
                variableDeclaration,
                Arrays.asList(
                        fromValue("hello"),
                        fromValue(0),
                        fromValue("test"),
                        fromValue(0)
                ),
                fromValue("New Value")
        );

        assertEquals(variableDeclaration.getVariable().getStringValue(), "[{\"test\":[{\"other\":4}]}]");

        assign.process(debugger);
        assertEquals(variableDeclaration.getVariable().getStringValue(), "[{\"test\":[{\"other\":4,\"hello\":\"New Value\"}]}]");
    }

    @Test
    public void testAssignWithDebuggerStop() {
        Debugger debugger = spy(new NullDebugger());
        doReturn(true).when(debugger).shouldStop();

        VariableDeclaration variableDeclaration = new VariableDeclaration(fromValue("Hello"), "testVar");
        variableDeclaration.process(debugger);

        assertTrue(variableDeclaration.getVariable().isNull());
    }

    private MetaExpression map(String key, MetaExpression value) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put(key, value);
        return fromValue(result);
    }

    private MetaExpression list(MetaExpression item) {
        List<MetaExpression> result = new ArrayList<>();
        result.add(item);
        return fromValue(result);
    }
}