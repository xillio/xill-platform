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
package nl.xillio.xill.plugins.collection.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;

/**
 * Test the {@link RemoveConstructConstruct}.
 *
 * @author Sander Visser
 */
public class RemoveConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process method under normal circumstances with list input.
     */
    @Test
    public void testProcessRemoveExistingElementFromList() {

        @SuppressWarnings("unchecked")
        ArrayList<MetaExpression> list = mock(ArrayList.class);

        // mock
        MetaExpression input = mock(MetaExpression.class);
        when(input.getType()).thenReturn(LIST);
        when(input.getValue()).thenReturn(list);

        int indexReturnValue = 1;
        MetaExpression index = mock(MetaExpression.class);
        when(index.getNumberValue()).thenReturn(indexReturnValue);
        when(index.getNumberValue().intValue()).thenReturn(indexReturnValue);

        when(list.size()).thenReturn(3);
        when(list.remove(1)).thenReturn(fromValue("a"));

        // run
        MetaExpression result = RemoveConstruct.process(input, index);

        // verify
        verify(list, times(1)).remove(indexReturnValue);
        verify(list, times(1)).size();

        // assert
        Assert.assertEquals(result, NULL);
    }

    /**
     * Test the process method under normal circumstances with object input.
     */
    @Test
    public void testProcessRemoveExistingElementFromObject() {

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, MetaExpression> obj = mock(LinkedHashMap.class);

        // mock
        MetaExpression input = mock(MetaExpression.class);
        when(input.getType()).thenReturn(OBJECT);
        when(input.getValue()).thenReturn(obj);

        String indexReturnValue = "test";
        MetaExpression index = mock(MetaExpression.class);
        when(index.getStringValue()).thenReturn(indexReturnValue);

        when(obj.containsKey(indexReturnValue)).thenReturn(true);
        when(obj.remove(indexReturnValue)).thenReturn(TRUE);

        // run
        MetaExpression result = RemoveConstruct.process(input, index);

        // verify
        verify(input, times(1)).getValue();
        verify(input, times(2)).getType();
        verify(obj, times(1)).remove(indexReturnValue);
        verify(obj, times(1)).containsKey(indexReturnValue);

        // assert
        Assert.assertEquals(result, NULL);
    }

    /**
     * Test the process method with object where the element it tries to remove does not exist.
     */
    @Test
    public void testProcessRemoveNotExistingElementFromObject() {

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, MetaExpression> obj = mock(LinkedHashMap.class);

        // mock
        MetaExpression input = mock(MetaExpression.class);
        when(input.getType()).thenReturn(OBJECT);
        when(input.getValue()).thenReturn(obj);

        String indexReturnValue = "test";
        MetaExpression index = mock(MetaExpression.class);
        when(index.getStringValue()).thenReturn(indexReturnValue);

        when(obj.containsKey(indexReturnValue)).thenReturn(false);
        when(obj.remove(indexReturnValue)).thenReturn(TRUE);

        // run
        MetaExpression result = RemoveConstruct.process(input, index);

        // verify
        verify(input, times(1)).getValue();
        verify(input, times(2)).getType();
        verify(obj, times(0)).remove(indexReturnValue);
        verify(obj, times(1)).containsKey(indexReturnValue);

        // assert
        Assert.assertEquals(result, NULL);
    }

    /**
     * Test the process method with index out of bounds
     *
     * @throws Throwable while testing
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Index is out of bounds: (.+)")
    public void testProcessIndexOutOfBounds() throws Throwable {
        // mock
        @SuppressWarnings("unchecked")
        ArrayList<MetaExpression> list = mock(ArrayList.class);

        MetaExpression input = mock(MetaExpression.class);
        when(input.getType()).thenReturn(LIST);
        when(input.getValue()).thenReturn(list);

        Number indexReturnValue = 8;
        MetaExpression index = mock(MetaExpression.class);
        when(index.getNumberValue()).thenReturn(indexReturnValue);
        when(index.getNumberValue().intValue()).thenReturn(indexReturnValue.intValue());

        when(list.size()).thenReturn(3);
        when(list.remove(indexReturnValue)).thenReturn(true);

        // Run method
        RemoveConstruct.process(input, index);
    }

}
