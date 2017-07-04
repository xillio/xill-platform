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
import nl.xillio.xill.api.NullDebugger;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.components.expressions.VariableAccessExpression;
import nl.xillio.xill.components.instructions.VariableDeclaration;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FromListTest extends TestUtils {
    private MetaExpression process(Processable list, Processable index) {
        return new FromList(list, index).process(new NullDebugger()).get();
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*'member'.*")
    public void testGetValueFromAtomic() {
        process(fromValue(true), fromValue("member"));
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*'member'.*value is null.*")
    public void testGetValueFromNull() {
        process(NULL, fromValue("member"));
    }

    @Test
    private void testGetValueFromList() {
        String element = "foo";
        MetaExpression result = process(createList(element), fromValue(0));
        assertEquals(result.getStringValue(), element);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Illegal value for list index: 1")
    private void testGetValueFromListIndexOutOfRange() {
        process(createList("something"), fromValue(1));
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*'nan'.*")
    private void testGetNanFromList() {
        process(createList(), fromValue("nan"));
    }

    @Test
    private void testGetValueFromObject() {
        String key = "foo";
        String value = "bar";
        MetaExpression result = process(createMap(key, value), fromValue(key));
        assertEquals(result.getStringValue(), value);
    }

    @Test
    private void testGetValueFromObjectNull() {
        MetaExpression result = process(createMap(), fromValue("no-key"));
        assertTrue(result.isNull());
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*'member'.*")
    private void TestGetValueFromAtomicInObject() {
        String key = "foo";
        process(process(createMap(key, "bar"), fromValue(key)), fromValue("member"));
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*'member'.*in chain starting with 'obj'.*")
    private void TestGetValueFromAtomicInObjectVariable() {
        String key = "foo";

        VariableDeclaration declaration = new VariableDeclaration(NULL, "obj");
        declaration.pushVariable(createMap(key, "bar"), 0);
        VariableAccessExpression var = new VariableAccessExpression(declaration);

        process(new FromList(var, fromValue(key)), fromValue("member"));
    }
}
