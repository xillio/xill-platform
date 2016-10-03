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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.exceptions.FailedToGetMatcherException;
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
public class AllMatchesConstructTest extends ExpressionBuilderHelper {
    private int timeoutValue = 10000;

    /**
     * Test the process method under normal circumstances.
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test
    public void processNormalUsage() throws IllegalArgumentException, FailedToGetMatcherException, JsonException {
        // Mock
        String text = "abc def ghi jkl. Mno";
        MetaExpression value = mock(MetaExpression.class);
        when(value.getStringValue()).thenReturn(text);

        String regexValue = "\\w+";
        MetaExpression regex = mock(MetaExpression.class);
        when(regex.getStringValue()).thenReturn(regexValue);

        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        String ReturnValue = String.format("[%1$s" +
                "  \"abc\",%1$s" +
                "  \"def\",%1$s" +
                "  \"ghi\",%1$s" +
                "  \"jkl\",%1$s" +
                "  \"Mno\"%1$s" +
                "]", System.getProperty("line.separator"));
        RegexService regexService = mock(RegexService.class);
        List<String> returnStatement = Arrays.asList("abc", "def", "ghi", "jkl", "Mno");
        when(regexService.tryMatch(any())).thenReturn(returnStatement);

        // Run
        MetaExpression result = AllMatchesConstruct.process(value, regex, timeout, regexService);

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
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid pattern: .*")
    public void processInvalidPattern() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String text = "abc def ghi jkl. Mno";
        MetaExpression value = mock(MetaExpression.class);
        when(value.getStringValue()).thenReturn(text);

        String regexValue = "\\w+";
        MetaExpression regex = mock(MetaExpression.class);
        when(regex.getStringValue()).thenReturn(regexValue);

        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        RegexService regexService = mock(RegexService.class);
        Arrays.asList("abc", "def", "ghi", "jkl", "Mno");
        when(regexService.getMatcher(regexValue, text, timeoutValue)).thenThrow(new PatternSyntaxException(regexValue, text, timeoutValue));

        AllMatchesConstruct.process(value, regex, timeout, regexService);

        // Verify
        verify(regexService, times(0)).tryMatch(any());
        verify(regexService, times(1)).getMatcher(regexValue, text, timeoutValue);
    }

    /**
     * Test the process method under normal circumstances.
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Illegal argument: .*")
    public void processIllegalArgument() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String text = "abc def ghi jkl. Mno";
        MetaExpression value = mock(MetaExpression.class);
        when(value.getStringValue()).thenReturn(text);

        String regexValue = "\\w+";
        MetaExpression regex = mock(MetaExpression.class);
        when(regex.getStringValue()).thenReturn(regexValue);
        
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        RegexService regexService = mock(RegexService.class);
        Arrays.asList("abc", "def", "ghi", "jkl", "Mno");
        when(regexService.getMatcher(regexValue, text, timeoutValue)).thenThrow(new IllegalArgumentException());

        AllMatchesConstruct.process(value, regex, timeout, regexService);

        // Verify
        verify(regexService, times(0)).tryMatch(any());
        verify(regexService, times(1)).getMatcher(regexValue, text, timeoutValue);
    }
}
