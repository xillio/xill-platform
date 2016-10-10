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

    ListExpression expression;
    List<MetaExpression> metas;

    public ListExpressionTest(){
        metas = new ArrayList<>();
        metas.add(new AtomicExpression(new BooleanBehavior(true)));
        metas.add(new AtomicExpression(new NumberBehavior(1)));
        List<MetaExpression> single = new ArrayList<>();
        single.add(new AtomicExpression(new StringBehavior("test")));
        metas.add(new ListExpression(single));
        expression = new ListExpression(metas);
    }

    @Test
    public void testGetChildren() throws Exception {
        assertTrue(expression.getChildren().size() == 3);
    }

    @Test
    public void testGetBooleanValue() {
        assertTrue(expression.getBooleanValue());
    }

    @Test
    public void testGetNumberValue() throws Exception {
        assertEquals(expression.getNumberValue(), Double.NaN);
    }

    @Test
    public void testCopy() throws Exception {
        ListExpression copy = expression.copy();

        assertEquals(copy.getSize(), expression.getSize());
        assertTrue(copy != expression);

        Collection<Processable> children = expression.getChildren();

        for(MetaExpression meta : metas)
            assertTrue(children.contains(meta));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCopyClosed() throws Exception{
        ListExpression copy = expression.copy();
        copy.close();
        copy.copy();
    }

    @Test
    public void testGetSize() throws Exception {
        assertEquals(expression.getSize(), 3);
    }

}
