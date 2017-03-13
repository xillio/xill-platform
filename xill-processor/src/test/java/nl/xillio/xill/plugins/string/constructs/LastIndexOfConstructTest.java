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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import nl.xillio.xill.plugins.string.services.string.StringUtilityServiceImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test the {@link LastIndexOfConstruct}
 */
public class LastIndexOfConstructTest extends TestUtils{

    private final static StringUtilityService stringUtilityService = new StringUtilityServiceImpl();
    private final static LastIndexOfConstruct lastIndexOfConstruct = new LastIndexOfConstruct(stringUtilityService);

    private int getPosition(String haystack, String needle){
        return process(lastIndexOfConstruct, fromValue(haystack), fromValue(needle)).getNumberValue().intValue();
    }

    private void test(String haystack, String needle, int expectedPosition){
        assertEquals(getPosition(haystack, needle), expectedPosition);
    }

    @Test
    public void testComputedIndexIsLastIndex(){
        test("a", "a", 0);
        test("aa", "a", 1);
        test("ab", "a", 0);
        test("ba", "a", 1);
        test("aba", "a", 2);

        test("AA", "AA", 0);
        test("AAA", "AA", 1);
        test("AAAA", "AA", 2);
        test("abAA", "AA", 2);
    }

    @Test
    public void testNoOccurrenceExistsReturnsMinusOne(){
        test("", "a", -1);
        test("abc", "d", -1);
    }

    @Test
    public void testUppercaseNotEqualLowerCase(){
        test("a", "A", -1);
        test("A", "a", -1);
    }

    @Test
    public void testEmptyNeedleReturnsLengthHaystack(){
        test("", "", 0);
        test("a", "", 1);
        test("abcdefghij", "", 10);
    }

    @Test (expectedExceptions = RobotRuntimeException.class)
    public void testNeedleIsNullThrowsError() throws Exception{
        process(lastIndexOfConstruct, fromValue("haystack"), NULL);
    }

    @Test (expectedExceptions = RobotRuntimeException.class)
    public void testHaystackIsNullThrowsError() throws Exception{
        process(lastIndexOfConstruct, NULL, fromValue("needle"));
    }
}