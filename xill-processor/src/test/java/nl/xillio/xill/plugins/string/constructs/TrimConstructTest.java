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
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import nl.xillio.xill.plugins.string.services.string.StringUtilityServiceImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the {@link TrimConstruct}.
 */
public class TrimConstructTest extends TestUtils {

    private static final StringUtilityService stringService = new StringUtilityServiceImpl();

    private TrimConstruct construct = new TrimConstruct(stringService);
    /**
     * Test the process method with a string given and internal set to false.
     */
    @Test
    public void processAtomicNoInternal() {

        // Run
        MetaExpression result = process(construct, fromValue("  test  ing   "), fromValue(false));

        // Assert
        Assert.assertEquals(result.getStringValue(), "test  ing");
    }

    /**
     * Test the process method with a string given and internal set to true.
     */
    @Test
    public void processAtomicInternal() {
        // Run
        MetaExpression result = process(construct, fromValue("  Test    test  \u00A0"), fromValue(true));

        // Assert
        Assert.assertEquals(result.getStringValue(), "Test test");
    }

    /**
     * Test the process with a list given and internal set to false
     */
    @Test
    public void processListNoInternal() {

        // Run
        MetaExpression result = process(construct, createTestList(), fromValue(false));

        // Assert
        Assert.assertEquals(result.getType(), LIST);

        @SuppressWarnings("unchecked")
        List<MetaExpression> resultAsList = (List<MetaExpression>) result.getValue();
        Assert.assertEquals(resultAsList.size(), 2);
        Assert.assertEquals(resultAsList.get(0).getStringValue(), "fi  rst");
        Assert.assertEquals(resultAsList.get(1).getStringValue(), "sec  ond");
    }

    /**
     * Test the process method with a list given and internal set to true.
     */
    @Test
    public void processListInternal() {

        // Run
        MetaExpression result = process(construct, createTestList(), fromValue(true));

        // Assert
        Assert.assertEquals(result.getType(), LIST);

        @SuppressWarnings("unchecked")
        List<MetaExpression> resultAsList = (List<MetaExpression>) result.getValue();
        Assert.assertEquals(resultAsList.size(), 2);
        Assert.assertEquals(resultAsList.get(0).getStringValue(), "fi rst");
        Assert.assertEquals(resultAsList.get(1).getStringValue(), "sec ond");

    }

    /**
     * Test the process method with a null value given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void processNullValue() {

        // Run
        process(construct, ExpressionBuilderHelper.NULL, fromValue(true));

        // Verify
        verify(stringService, times(0)).trim(anyString());
        verify(stringService, times(0)).trimInternal(anyString());
    }

    private MetaExpression createTestList() {
        MetaExpression first = fromValue("  fi  rst  ");
        MetaExpression second = fromValue("sec\u00A0\u00A0ond\u00A0 ");
        List<MetaExpression> listValue = Arrays.asList(first, second);
        return fromValue(listValue);
    }
}
