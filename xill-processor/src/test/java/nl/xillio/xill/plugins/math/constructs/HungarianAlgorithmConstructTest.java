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
package nl.xillio.xill.plugins.math.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link HungarianAlgorithmConstruct}.
 */
public class HungarianAlgorithmConstructTest extends TestUtils {

    /**
     * <p>
     * Test the process method under normal circumstances.
     * </p>
     * <p>
     * We not only test if the output has the correct type but also the correct value.
     * </p>
     * <b>Nothing to verify</b>
     */
    @Test
    public void processNormalUsage() {
        // Mock
        MetaExpression matrix = mock(MetaExpression.class);
        String hungarianReturnValue = String.format("[%1$s" +
                "  {%1$s" +
                "    \"sum\" : 10.0%1$s" +
                "  },%1$s" +
                "  {%1$s" +
                "    \"cells\" : [%1$s" +
                "      {%1$s" +
                "        \"row\" : 0,%1$s" +
                "        \"col\" : 2%1$s" +
                "      },%1$s" +
                "      {%1$s" +
                "        \"row\" : 1,%1$s" +
                "        \"col\" : 1%1$s" +
                "      },%1$s" +
                "      {%1$s" +
                "        \"row\" : 2,%1$s" +
                "        \"col\" : 0%1$s" +
                "      }%1$s" +
                "    ]%1$s" +
                "  }%1$s" +
                "]", System.getProperty("line.separator"));
        when(matrix.getType()).thenReturn(LIST);
        when(matrix.getValue()).thenReturn(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1), fromValue(3))),
                fromValue(Arrays.asList(fromValue(2), fromValue(2), fromValue(3))),
                fromValue(Arrays.asList(fromValue(5), fromValue(4), fromValue(1)))));

        MetaExpression max = mock(MetaExpression.class);
        when(max.getBooleanValue()).thenReturn(true);

        // Run
        MetaExpression result = HungarianAlgorithmConstruct.process(matrix, max);

        // Verify
        // nothing to verify

        // Assert
        // Check wheter the result is correct as String value.
        Assert.assertEquals(result.toString(), hungarianReturnValue);

        // Check if the result is a list, if so, check if its children are objects
        Assert.assertEquals(result.getType(), LIST);
        @SuppressWarnings("unchecked")
        List<MetaExpression> resultAsList = (List<MetaExpression>) result.getValue();
        Assert.assertEquals(resultAsList.size(), 2);
        Assert.assertEquals(resultAsList.get(0).getType(), OBJECT);
        Assert.assertEquals(resultAsList.get(1).getType(), OBJECT);

        // Check wheter the second object contains a list, if so, check if its children are objects
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, MetaExpression> secondItem = (LinkedHashMap<String, MetaExpression>) resultAsList.get(1).getValue();
        Assert.assertEquals(secondItem.get("cells").getType(), LIST);
        @SuppressWarnings("unchecked")
        List<MetaExpression> secondItemAsList = (List<MetaExpression>) secondItem.get("cells").getValue();
        Assert.assertEquals(secondItemAsList.size(), 3);
        Assert.assertEquals(secondItemAsList.get(0).getType(), OBJECT);
        Assert.assertEquals(secondItemAsList.get(1).getType(), OBJECT);
        Assert.assertEquals(secondItemAsList.get(2).getType(), OBJECT);
    }

    /**
     * Tests whether the exception is thrown when there are too little rows provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Not enough data..*At least 1 row with data..*")
    public void processTooFewRows() {

        MetaExpression matrix = mock(MetaExpression.class);
        when(matrix.getType()).thenReturn(LIST);
        when(matrix.getValue()).thenReturn(Arrays.asList());

        MetaExpression max = mock(MetaExpression.class);
        when(max.getBooleanValue()).thenReturn(true);

        HungarianAlgorithmConstruct.process(matrix, max);
    }

    /**
     * Tests whether the exception is thrown when enough rows but too little columns are provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Not enough data..*At least 1 column with data..*")
    public void processTooFewColumns() {
        MetaExpression matrix = mock(MetaExpression.class);
        when(matrix.getType()).thenReturn(LIST);
        when(matrix.getValue()).thenReturn(Arrays.asList(fromValue(Arrays.asList())));

        MetaExpression max = mock(MetaExpression.class);
        when(max.getBooleanValue()).thenReturn(true);

        HungarianAlgorithmConstruct.process(matrix, max);
    }

    /**
     * Tests whether an exception is thrown when invalid input is provided.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid value `error` in matrix at \\[1,1\\]")
    public void processInvalidInputInMatrix() {
        // Mock
        MetaExpression matrix = mock(MetaExpression.class);
        when(matrix.getType()).thenReturn(LIST);
        when(matrix.getValue()).thenReturn(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1), fromValue(3))),
                fromValue(Arrays.asList(fromValue(2), fromValue("error"), fromValue(3))),
                fromValue(Arrays.asList(fromValue(5), fromValue(4), fromValue(1)))));

        MetaExpression max = mock(MetaExpression.class);
        when(max.getBooleanValue()).thenReturn(true);
        // Run
        HungarianAlgorithmConstruct.process(matrix, max);
    }
}
