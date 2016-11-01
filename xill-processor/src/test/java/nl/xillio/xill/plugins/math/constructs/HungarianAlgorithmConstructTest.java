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
    private HungarianAlgorithmConstruct construct = new HungarianAlgorithmConstruct();

    /**
     * Test the process method under normal circumstances.
     * We not only test if the output has the correct type but also the correct value.
     */
    @Test
    public void processNormalUsage() {
        String expectedString = "{\"sum\":10.0,\"cells\":[{\"row\":0,\"col\":2},{\"row\":1,\"col\":1},{\"row\":2,\"col\":0}]}";
        MetaExpression matrix = fromValue(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1), fromValue(3))),
                fromValue(Arrays.asList(fromValue(2), fromValue(2), fromValue(3))),
                fromValue(Arrays.asList(fromValue(5), fromValue(4), fromValue(1)))
        ));

        // Run.
        MetaExpression result = this.process(construct, matrix, fromValue(true));

        // Assert.
        Assert.assertEquals(result.toString(), expectedString);

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
     * Test the process when the matrix has more columns than rows, this means it has to be transposed.
     */
    @Test
    public void processTranspose() {
        String expectedString = "{\"sum\":2.0,\"cells\":[{\"row\":0,\"col\":0},{\"row\":2,\"col\":1}]}";
        MetaExpression matrix = fromValue(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1))),
                fromValue(Arrays.asList(fromValue(2), fromValue(4))),
                fromValue(Arrays.asList(fromValue(5), fromValue(2)))
        ));

        // Run.
        MetaExpression result = this.process(construct, matrix, fromValue(false));

        // Assert.
        Assert.assertEquals(result.toString(), expectedString);
    }

    /**
     * Tests whether the exception is thrown when there are too little rows provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Not enough data\\..*At least 1 row with data\\..*")
    public void processTooFewRows() {
        this.process(construct, emptyList());
    }

    /**
     * Tests whether the exception is thrown when enough rows but too little columns are provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Not enough data\\..*At least 1 column with data\\..*")
    public void processTooFewColumns() {
        MetaExpression matrix = fromValue(Collections.singletonList(emptyList()));

        this.process(construct, matrix);
    }

    /**
     * Tests whether an exception is thrown when invalid input is provided.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid value at \\[1,1\\].*")
    public void processInvalidInputInMatrix() {
        MetaExpression matrix = fromValue(Arrays.asList(
                fromValue(Arrays.asList(fromValue(0), fromValue(1))),
                fromValue(Arrays.asList(fromValue(2), fromValue("error")))
        ));

        this.process(construct, matrix);
    }
}
