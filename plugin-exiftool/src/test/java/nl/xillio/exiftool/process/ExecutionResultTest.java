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
package nl.xillio.exiftool.process;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;


public class ExecutionResultTest {

    /**
     * This test will test if the iterator stops whenever the stream ends.
     */
    @Test
    public void testEndOfStreamExit() {
        String input = "Line 1\nLine 2\r\nLine 3\rEnd";

        ExecutionResult result = new ExecutionResult(
                new BufferedReader(new StringReader(input)),
                () -> {
                },
                "The End String"
        );

        List<String> lines = new ArrayList<>();
        result.forEachRemaining(lines::add);

        assertEquals(lines, Arrays.asList("Line 1", "Line 2", "Line 3", "End"));
    }

    /**
     * Test if the kill string will terminate the stream and not be included.
     */
    @Test
    public void testKillStringExit() {
        String input = "Line 1\nLine 2\r\nLine 3\rEnd";

        ExecutionResult executionResult = new ExecutionResult(
                new BufferedReader(new StringReader(input)),
                () -> {
                },
                "End");

        List<String> lines = new ArrayList<>();
        executionResult.forEachRemaining(lines::add);

        assertEquals(lines, Arrays.asList("Line 1", "Line 2", "Line 3"));
    }

}