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
package nl.xillio.xill.plugins.math.services;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.math.services.math.MathOperationsImpl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Unit test class for the service MathOperationsImpl.
 *
 * @author Pieter Soels.
 */
public class MathOperationsImplTest extends TestUtils {

    @DataProvider(name = "isNumber")
    Object[][] isNumberTypes() {
        return new Object[][]{
                {fromValue(5.05), true},
                {fromValue("1"), true},
                {fromValue("Test"), false},
                {fromValue(Double.NEGATIVE_INFINITY), true},
                {fromValue(Double.POSITIVE_INFINITY), true},
                {fromValue(true), true},
                {fromValue((String) null), false},
                {emptyList(), false},
                {emptyObject(), false},
                {fromValue(initializeTestList()), false},
                {fromValue(initializeTestObject()), false},
                {fromValue(ZonedDateTime.now().toString()), false}
        };
    }

    private LinkedHashMap<String, MetaExpression> initializeTestObject() {
        LinkedHashMap<String, MetaExpression> value = new LinkedHashMap<>();
        value.put("test", fromValue(1));
        return value;
    }

    private List<MetaExpression> initializeTestList() {
        List<MetaExpression> value = new ArrayList<>();
        value.add(fromValue(1));
        return value;
    }

    @Test(dataProvider = "isNumber")
    public void testIsNumber(MetaExpression value, boolean result) {
        // Initialize
        MathOperationsImpl math = new MathOperationsImpl();

        // Assert
        Assert.assertEquals(math.isNumber(value), result);
    }
}
