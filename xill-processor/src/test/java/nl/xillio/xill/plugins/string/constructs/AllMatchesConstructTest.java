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
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.RegexService;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link AllMatchesConstruct}.
 */
public class AllMatchesConstructTest extends TestUtils {
    private int timeoutValue = 10000;

    /**
     * Test the process method under normal circumstances.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test
    public void processNormalUsage() throws IllegalArgumentException, JsonException {
        // Mock
        String text = "abc def ghi jkl. Mno";
        MetaExpression value = mockExpression(ATOMIC);
        when(value.getStringValue()).thenReturn(text);

        String regexValue = "\\w+";
        MetaExpression regex = mockExpression(ATOMIC);
        when(regex.getStringValue()).thenReturn(regexValue);

        MetaExpression timeout = mockExpression(ATOMIC);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        String ReturnValue = "[\"abc\",\"def\",\"ghi\",\"jkl\",\"Mno\"]";
        RegexService regexService = mock(RegexService.class);
        List<String> returnStatement = Arrays.asList("abc", "def", "ghi", "jkl", "Mno");
        when(regexService.tryMatch(any())).thenReturn(returnStatement);

        AllMatchesConstruct construct = new AllMatchesConstruct(regexService);
        // Run
        MetaExpression result = process(construct, value, regex, timeout);

        // Verify
        verify(regexService, times(1)).tryMatch(any());
        verify(regexService, times(1)).getMatcher(regexValue, text, timeoutValue);

        // Assert
        Assert.assertEquals(result.getType(), LIST);
        Assert.assertEquals(result.toString(new JacksonParser(false)), ReturnValue);
    }

    /**
     * Test the process method under normal circumstances.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid pattern: .*")
    public void processInvalidPattern() throws IllegalArgumentException {
        // Mock
        String text = "abc def ghi jkl. Mno";
        MetaExpression value = mockExpression(ATOMIC);
        when(value.getStringValue()).thenReturn(text);

        String regexValue = "\\w+";
        MetaExpression regex = mockExpression(ATOMIC);
        when(regex.getStringValue()).thenReturn(regexValue);

        MetaExpression timeout = mockExpression(ATOMIC);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        RegexService regexService = mock(RegexService.class);
        Arrays.asList("abc", "def", "ghi", "jkl", "Mno");
        when(regexService.getMatcher(regexValue, text, timeoutValue)).thenThrow(new PatternSyntaxException(regexValue, text, timeoutValue));

        AllMatchesConstruct construct = new AllMatchesConstruct(regexService);
        // Run
        process(construct, value, regex, timeout);

        // Verify
        verify(regexService, times(0)).tryMatch(any());
        verify(regexService, times(1)).getMatcher(regexValue, text, timeoutValue);
    }

    /**
     * Test the process method under normal circumstances.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Illegal argument: .*")
    public void processIllegalArgument() throws IllegalArgumentException {
        // Mock
        String text = "abc def ghi jkl. Mno";
        MetaExpression value = mockExpression(ATOMIC);
        when(value.getStringValue()).thenReturn(text);

        String regexValue = "\\w+";
        MetaExpression regex = mockExpression(ATOMIC);
        when(regex.getStringValue()).thenReturn(regexValue);

        MetaExpression timeout = mockExpression(ATOMIC);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        RegexService regexService = mock(RegexService.class);
        Arrays.asList("abc", "def", "ghi", "jkl", "Mno");
        when(regexService.getMatcher(regexValue, text, timeoutValue)).thenThrow(new IllegalArgumentException());

        AllMatchesConstruct construct = new AllMatchesConstruct(regexService);
        // Run
        process(construct, value, regex, timeout);

        // Verify
        verify(regexService, times(0)).tryMatch(any());
        verify(regexService, times(1)).getMatcher(regexValue, text, timeoutValue);
    }
}
