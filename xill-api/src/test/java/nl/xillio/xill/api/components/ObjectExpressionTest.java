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
package nl.xillio.xill.api.components;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.testng.Assert.*;

/**
 * This class tests the {@link ObjectExpression} class.
 *
 * @author Daan Knoope
 */
public class ObjectExpressionTest {

    ObjectExpression objectExpression;
    LinkedHashMap<String, MetaExpression> value;

    // Fill objectExpression with interesting content
    public ObjectExpressionTest(){
        value = new LinkedHashMap<>();
        value.put("expr1", new AtomicExpression(true));
        value.put("expr2", new AtomicExpression(2));
        value.put("expr3", new AtomicExpression("string"));

        ArrayList<MetaExpression> listContent = new ArrayList<>();
        listContent.add(new AtomicExpression(false));
        listContent.add(new AtomicExpression(-4));
        listContent.add(new AtomicExpression("string2"));
        value.put("expr4", new ListExpression(listContent));

        LinkedHashMap<String, MetaExpression> expr5 = new LinkedHashMap<>();
        expr5.put("sub1", new AtomicExpression(true));
        expr5.put("sub2", new AtomicExpression("string3"));
        value.put("expr5", new ObjectExpression(expr5));

        objectExpression = new ObjectExpression(value);
        int i = 0;
    }

    @Test
    public void testProcess() throws Exception {

    }

    @Test
    public void testCopy() throws Exception {
        MetaExpression copy = objectExpression.copy();
        assertTrue(copy != objectExpression);
        assertEquals(objectExpression.getSize(), copy.getSize());
    }

    @Test (expectedExceptions = IllegalStateException.class)
    public void testCopyClosed() throws Exception{
        MetaExpression copy = objectExpression.copy();
        copy.close();
        copy.copy();
    }

    @Test
    public void testGetChildren() throws Exception {
        List<Processable> children = (List<Processable>) objectExpression.getChildren();
        assertSame(children.get(0), value.get("expr1"));
        assertSame(children.get(1), value.get("expr2"));
        assertSame(children.get(2), value.get("expr3"));
        assertSame(children.get(3), value.get("expr4"));
        assertSame(children.get(4), value.get("expr5"));
    }

    @Test
    public void testGetNumberValue() throws Exception {
        assertEquals(objectExpression.getNumberValue(), Double.NaN);
    }

    @Test
    public void testGetSize() throws Exception {
        assertEquals(objectExpression.getSize(), value.size());
    }

}
