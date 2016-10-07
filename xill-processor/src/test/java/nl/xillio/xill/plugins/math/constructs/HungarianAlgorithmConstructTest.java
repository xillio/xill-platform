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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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
        String hungarianReturnValue = "{\"sum\":10.0,\"cells\":[{\"row\":0,\"col\":2},{\"row\":1,\"col\":1},{\"row\":2,\"col\":0}]}";

        // Variables.
        MetaExpression matrix = fromValue(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1), fromValue(3))),
                fromValue(Arrays.asList(fromValue(2), fromValue(2), fromValue(3))),
                fromValue(Arrays.asList(fromValue(5), fromValue(4), fromValue(1)))
        ));
        MetaExpression max = fromValue(true);

        // Run.
        MetaExpression result = HungarianAlgorithmConstruct.process(matrix, max);

        // Assert.
        Assert.assertEquals(result.toString(), hungarianReturnValue);

        // Check if the result is an object and if the children are the correct type.
        Assert.assertEquals(result.getType(), OBJECT);
        LinkedHashMap<String, MetaExpression> resultMap = result.getValue();
        Assert.assertEquals(resultMap.size(), 2);
        Assert.assertEquals(resultMap.get("sum").getType(), ATOMIC);
        Assert.assertEquals(resultMap.get("cells").getType(), LIST);

        // Check if all cells are objects.
        List<MetaExpression> cellsList = resultMap.get("cells").getValue();
        Assert.assertEquals(cellsList.size(), 3);
        Assert.assertEquals(cellsList.get(0).getType(), OBJECT);
        Assert.assertEquals(cellsList.get(1).getType(), OBJECT);
        Assert.assertEquals(cellsList.get(2).getType(), OBJECT);
    }

    /**
     * Tests whether the exception is thrown when there are too little rows provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Not enough data\\..*At least 1 row with data\\..*")
    public void processTooFewRows() {
        HungarianAlgorithmConstruct.process(emptyList(), fromValue(true));
    }

    /**
     * Tests whether the exception is thrown when enough rows but too little columns are provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Not enough data\\..*At least 1 column with data\\..*")
    public void processTooFewColumns() {
        MetaExpression matrix = fromValue(Collections.singletonList(emptyList()));

        HungarianAlgorithmConstruct.process(matrix, fromValue(true));
    }

    /**
     * Tests whether an exception is thrown when invalid input is provided.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid value `error` in matrix at \\[1,1\\]")
    public void processInvalidInputInMatrix() {
        MetaExpression matrix = fromValue(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1))),
                fromValue(Arrays.asList(fromValue(2), fromValue("error")))
        ));

        HungarianAlgorithmConstruct.process(matrix, fromValue(true));
    }
}
