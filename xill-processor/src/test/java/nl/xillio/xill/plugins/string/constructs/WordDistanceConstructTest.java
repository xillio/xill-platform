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

import com.sun.org.apache.xpath.internal.operations.Bool;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

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

        int distanceValue = 6;
        int maxLength = 11;
        StringUtilityService stringService = mock(StringUtilityService.class);

        //Run
        WordDistanceConstruct construct = new WordDistanceConstruct(stringService);
        MetaExpression result = process(construct, source, target, relative);

        // Assert
        Assert.assertEquals(result.getNumberValue().intValue(), distanceValue / maxLength);
    }

    /**
     * Test the process method without relative distance.
     */
    @Test
    public void processNullUsage() {

        // Mock
        String sourceValue = "Lumberjack";
        MetaExpression source = mockExpression(ATOMIC);
        when(source.getStringValue()).thenReturn(sourceValue);

        String targetValue = "Lumbersnack";
        MetaExpression target = mockExpression(ATOMIC);
        when(target.getStringValue()).thenReturn(targetValue);

        boolean relativeValue = false;
        MetaExpression relative = mockExpression(ATOMIC);
        when(relative.getBooleanValue()).thenReturn(relativeValue);

        int returnValue = 6;
        StringUtilityService stringService = mock(StringUtilityService.class);

        //Run
        WordDistanceConstruct construct = new WordDistanceConstruct(stringService);
        MetaExpression result = process(construct, source, target, relative);

        // Assert
        Assert.assertEquals(result.getNumberValue().intValue(), returnValue);
    }
}
