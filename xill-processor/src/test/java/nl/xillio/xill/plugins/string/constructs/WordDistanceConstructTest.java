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
package nl.xillio.xill.plugins.string.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * Test the {@link RepeatConstruct}.
 */
public class WordDistanceConstructTest extends TestUtils {

    /**
     * Test the process method with relative distance.
     */
    @Test
    public void processRelativeUsage() {
        // Mock
        String sourceValue = "Lumberjack";
        MetaExpression source = mockExpression(ATOMIC);
        when(source.getStringValue()).thenReturn(sourceValue);

        String targetValue = "Lumbersnack";
        MetaExpression target = mockExpression(ATOMIC);
        when(target.getStringValue()).thenReturn(targetValue);

        boolean relativeValue = true;
        MetaExpression relative = mockExpression(ATOMIC);
        when(relative.getBooleanValue()).thenReturn(relativeValue);

        double distanceValue = 1 - ((double) 2 / 11);

        //Run
        WordDistanceConstruct construct = new WordDistanceConstruct();
        MetaExpression result = process(construct, source, target, relative);

        // Assert
        Assert.assertEquals(result.getNumberValue(), distanceValue);
    }

    /**
     * Test the process method without relative distance.
     */
    @Test
    public void processAbsoluteUsage() {

        // Mock
        String sourceValue = "Lumberjack";
        MetaExpression source = mockExpression(ATOMIC);
        when(source.getStringValue()).thenReturn(sourceValue);

        String targetValue = "Lmuberajck";
        MetaExpression target = mockExpression(ATOMIC);
        when(target.getStringValue()).thenReturn(targetValue);

        boolean relativeValue = false;
        MetaExpression relative = mockExpression(ATOMIC);
        when(relative.getBooleanValue()).thenReturn(relativeValue);

        int returnValue = 2;

        //Run
        WordDistanceConstruct construct = new WordDistanceConstruct();
        MetaExpression result = process(construct, source, target, relative);

        // Assert
        Assert.assertEquals(result.getNumberValue().intValue(), returnValue);
    }

    /**
     * Test the process method when source is empty.
     */
    @Test
    public void processEmptySourceUsage() {

        // Mock
        String sourceValue = "";
        MetaExpression source = mockExpression(ATOMIC);
        when(source.getStringValue()).thenReturn(sourceValue);

        String targetValue = "Lumbersnack";
        MetaExpression target = mockExpression(ATOMIC);
        when(target.getStringValue()).thenReturn(targetValue);

        boolean relativeValue = false;
        MetaExpression relative = mockExpression(ATOMIC);
        when(relative.getBooleanValue()).thenReturn(relativeValue);

        int returnValue = 11;

        //Run
        WordDistanceConstruct construct = new WordDistanceConstruct();
        MetaExpression result = process(construct, source, target, relative);

        // Assert
        Assert.assertEquals(result.getNumberValue().intValue(), returnValue);
    }

    /**
     * Test the process method when target is empty.
     */
    @Test
    public void processEmptyTargetUsage() {

        // Mock
        String sourceValue = "Lumberjack";
        MetaExpression source = mockExpression(ATOMIC);
        when(source.getStringValue()).thenReturn(sourceValue);

        String targetValue = "";
        MetaExpression target = mockExpression(ATOMIC);
        when(target.getStringValue()).thenReturn(targetValue);

        boolean relativeValue = false;
        MetaExpression relative = mockExpression(ATOMIC);
        when(relative.getBooleanValue()).thenReturn(relativeValue);

        int returnValue = 10;

        //Run
        WordDistanceConstruct construct = new WordDistanceConstruct();
        MetaExpression result = process(construct, source, target, relative);

        // Assert
        Assert.assertEquals(result.getNumberValue().intValue(), returnValue);
    }
}
