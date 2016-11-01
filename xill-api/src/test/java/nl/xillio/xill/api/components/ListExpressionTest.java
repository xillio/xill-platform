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

import nl.xillio.xill.services.json.JacksonParser;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the {@link ListExpression} class.
 *
 * @author Daan Knoope
 */
public class ListExpressionTest {

    private ListExpression expression;
    private List<MetaExpression> metas;

    private ListExpression empty;

    public ListExpressionTest(){
        metas = new ArrayList<>();
        metas.add(new AtomicExpression(new BooleanBehavior(true)));
        metas.add(new AtomicExpression(new NumberBehavior(1)));
        List<MetaExpression> single = new ArrayList<>();
        single.add(new AtomicExpression(new StringBehavior("test")));
        metas.add(new ListExpression(single));
        expression = new ListExpression(metas);
        empty = new ListExpression(new ArrayList<>());
    }

    @Test
    public void testGetChildren() {
        assertTrue(expression.getChildren().size() == 3);
        assertTrue(empty.getChildren().isEmpty());
    }

    @Test
    public void testGetBooleanValue() {
        assertTrue(expression.getBooleanValue());
        assertTrue(empty.getBooleanValue());
    }

    @Test
    public void testGetNumberValue() {
        assertEquals(expression.getNumberValue(), Double.NaN);
        assertEquals(empty.getNumberValue(), Double.NaN);
    }

    @Test
    public void testCopy() {
        MetaExpression copy = expression.copy();

        assertEquals(copy.getSize(), expression.getSize());
        assertTrue(copy != expression);

        Collection<Processable> children = expression.getChildren();

        for(MetaExpression meta : metas)
            assertTrue(children.contains(meta));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCopyClosed() {
        MetaExpression copy = expression.copy();
        copy.close();
        copy.copy();
    }

    @Test
    public void testGetSize() {
        assertEquals(expression.getSize(), 3);
    }

}
