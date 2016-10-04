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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.services.string.RegexService;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.PatternSyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link ReplaceConstruct}.
 */
public class ReplaceConstructTest {
    private int timeoutValue = 10000;
    
    /**
     * Test the process method when we use regex and want to replace all.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test
    public void processUseRegexReplaceAll() throws IllegalArgumentException {
        // Mock
        String textValue = "ReplaceR";
        MetaExpression text = mock(MetaExpression.class);
        when(text.getStringValue()).thenReturn(textValue);

        String needleValue = "R";
        MetaExpression needle = mock(MetaExpression.class);
        when(needle.getStringValue()).thenReturn(needleValue);

        String replacementValue = "O";
        MetaExpression replacement = mock(MetaExpression.class);
        when(replacement.getStringValue()).thenReturn(replacementValue);

        boolean useRegexValue = true;
        MetaExpression useRegex = mock(MetaExpression.class);
        when(useRegex.getBooleanValue()).thenReturn(useRegexValue);

        boolean replaceAllValue = true;
        MetaExpression replaceAll = mock(MetaExpression.class);
        when(replaceAll.getBooleanValue()).thenReturn(replaceAllValue);
        
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        String returnValue = "OeplaceO";
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.replaceAll(any(), eq(replacementValue))).thenReturn(returnValue);

        // Run
        MetaExpression args[] = {text, needle, replacement, useRegex, replaceAll, timeout};
        MetaExpression result = ReplaceConstruct.process(args, regexService, stringService);

        // Verify
        verify(regexService, times(1)).getMatcher(needleValue, textValue, timeoutValue);
        verify(regexService, times(1)).replaceAll(any(), eq(replacementValue));
        verify(regexService, times(0)).replaceFirst(any(), eq(replacementValue));
        verify(stringService, times(0)).replaceAll(textValue, needleValue, replacementValue);
        verify(stringService, times(0)).replaceFirst(textValue, needleValue, replacementValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method when we use regex and want to replace the first
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test
    public void processUseRegexReplaceFirst() throws IllegalArgumentException {
        // Mock
        String textValue = "ReplaceR";
        MetaExpression text = mock(MetaExpression.class);
        when(text.getStringValue()).thenReturn(textValue);

        String needleValue = "R";
        MetaExpression needle = mock(MetaExpression.class);
        when(needle.getStringValue()).thenReturn(needleValue);

        String replacementValue = "O";
        MetaExpression replacement = mock(MetaExpression.class);
        when(replacement.getStringValue()).thenReturn(replacementValue);

        boolean useRegexValue = true;
        MetaExpression useRegex = mock(MetaExpression.class);
        when(useRegex.getBooleanValue()).thenReturn(useRegexValue);

        boolean replaceAllValue = false;
        MetaExpression replaceAll = mock(MetaExpression.class);
        when(replaceAll.getBooleanValue()).thenReturn(replaceAllValue);
        
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        String returnValue = "OeplaceO";
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.replaceFirst(any(), eq(replacementValue))).thenReturn(returnValue);

        // Run
        MetaExpression args[] = {text, needle, replacement, useRegex, replaceAll, timeout};
        MetaExpression result = ReplaceConstruct.process(args, regexService, stringService);

        // Verify
        verify(regexService, times(1)).getMatcher(needleValue, textValue, timeoutValue);
        verify(regexService, times(0)).replaceAll(any(), eq(replacementValue));
        verify(regexService, times(1)).replaceFirst(any(), eq(replacementValue));
        verify(stringService, times(0)).replaceAll(textValue, needleValue, replacementValue);
        verify(stringService, times(0)).replaceFirst(textValue, needleValue, replacementValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method when we don't use regex and want to replace all.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test
    public void processDontUseRegexReplaceAll() throws IllegalArgumentException {
        // Mock
        String textValue = "ReplaceR";
        MetaExpression text = mock(MetaExpression.class);
        when(text.getStringValue()).thenReturn(textValue);

        String needleValue = "R";
        MetaExpression needle = mock(MetaExpression.class);
        when(needle.getStringValue()).thenReturn(needleValue);

        String replacementValue = "O";
        MetaExpression replacement = mock(MetaExpression.class);
        when(replacement.getStringValue()).thenReturn(replacementValue);

        boolean useRegexValue = false;
        MetaExpression useRegex = mock(MetaExpression.class);
        when(useRegex.getBooleanValue()).thenReturn(useRegexValue);

        boolean replaceAllValue = true;
        MetaExpression replaceAll = mock(MetaExpression.class);
        when(replaceAll.getBooleanValue()).thenReturn(replaceAllValue);
        
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        String returnValue = "OeplaceO";
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.replaceAll(textValue, needleValue, replacementValue)).thenReturn(returnValue);

        // Run
        MetaExpression args[] = {text, needle, replacement, useRegex, replaceAll, timeout};
        MetaExpression result = ReplaceConstruct.process(args, regexService, stringService);

        // Verify
        verify(regexService, times(0)).getMatcher(needleValue, textValue, timeoutValue);
        verify(regexService, times(0)).replaceAll(any(), eq(replacementValue));
        verify(regexService, times(0)).replaceFirst(any(), eq(replacementValue));
        verify(stringService, times(1)).replaceAll(textValue, needleValue, replacementValue);
        verify(stringService, times(0)).replaceFirst(textValue, needleValue, replacementValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method when we don't use regex and want to replace only the first.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test
    public void processDontUseRegexReplaceFirst() throws IllegalArgumentException {
        // Mock
        String textValue = "ReplaceR";
        MetaExpression text = mock(MetaExpression.class);
        when(text.getStringValue()).thenReturn(textValue);

        String needleValue = "R";
        MetaExpression needle = mock(MetaExpression.class);
        when(needle.getStringValue()).thenReturn(needleValue);

        String replacementValue = "O";
        MetaExpression replacement = mock(MetaExpression.class);
        when(replacement.getStringValue()).thenReturn(replacementValue);

        boolean useRegexValue = false;
        MetaExpression useRegex = mock(MetaExpression.class);
        when(useRegex.getBooleanValue()).thenReturn(useRegexValue);

        boolean replaceAllValue = false;
        MetaExpression replaceAll = mock(MetaExpression.class);
        when(replaceAll.getBooleanValue()).thenReturn(replaceAllValue);
        
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        String returnValue = "OeplaceO";
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(stringService.replaceFirst(textValue, needleValue, replacementValue)).thenReturn(returnValue);

        // Run
        MetaExpression args[] = {text, needle, replacement, useRegex, replaceAll, timeout};
        MetaExpression result = ReplaceConstruct.process(args, regexService, stringService);

        // Verify
        verify(regexService, times(0)).getMatcher(needleValue, textValue, timeoutValue);
        verify(regexService, times(0)).replaceAll(any(), eq(replacementValue));
        verify(regexService, times(0)).replaceFirst(any(), eq(replacementValue));
        verify(stringService, times(0)).replaceAll(textValue, needleValue, replacementValue);
        verify(stringService, times(1)).replaceFirst(textValue, needleValue, replacementValue);

        // Assert
        Assert.assertEquals(result.getStringValue(), returnValue);
    }

    /**
     * Test the process method when getMatcher throws a PatternSyntax
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid pattern in regex\\(\\).*")
    public void processInvalidException() throws IllegalArgumentException {
        // Mock
        String textValue = "ReplaceR";
        MetaExpression text = mock(MetaExpression.class);
        when(text.getStringValue()).thenReturn(textValue);

        String needleValue = "R";
        MetaExpression needle = mock(MetaExpression.class);
        when(needle.getStringValue()).thenReturn(needleValue);

        String replacementValue = "O";
        MetaExpression replacement = mock(MetaExpression.class);
        when(replacement.getStringValue()).thenReturn(replacementValue);

        boolean useRegexValue = true;
        MetaExpression useRegex = mock(MetaExpression.class);
        when(useRegex.getBooleanValue()).thenReturn(useRegexValue);

        boolean replaceAllValue = true;
        MetaExpression replaceAll = mock(MetaExpression.class);
        when(replaceAll.getBooleanValue()).thenReturn(replaceAllValue);
        
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        Exception returnValue = new PatternSyntaxException(needleValue, textValue, timeoutValue);
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.getMatcher(needleValue, textValue, timeoutValue)).thenThrow(returnValue);

        // Run
        MetaExpression args[] = {text, needle, replacement, useRegex, replaceAll, timeout};
        ReplaceConstruct.process(args, regexService, stringService);

        // Verify
        verify(regexService, times(1)).getMatcher(needleValue, textValue, timeoutValue);
        verify(regexService, times(0)).replaceAll(any(), eq(replacementValue));
        verify(regexService, times(0)).replaceFirst(any(), eq(replacementValue));
        verify(stringService, times(0)).replaceAll(textValue, needleValue, replacementValue);
        verify(stringService, times(0)).replaceFirst(textValue, needleValue, replacementValue);
    }

    /**
     * Test the method when getMatcher returns an illegalArgumentException.
     *
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Error while executing the regex")
    public void processIllegalArgumentException() throws IllegalArgumentException {
        // Mock
        String textValue = "ReplaceR";
        MetaExpression text = mock(MetaExpression.class);
        when(text.getStringValue()).thenReturn(textValue);

        String needleValue = "R";
        MetaExpression needle = mock(MetaExpression.class);
        when(needle.getStringValue()).thenReturn(needleValue);

        String replacementValue = "O";
        MetaExpression replacement = mock(MetaExpression.class);
        when(replacement.getStringValue()).thenReturn(replacementValue);

        boolean useRegexValue = true;
        MetaExpression useRegex = mock(MetaExpression.class);
        when(useRegex.getBooleanValue()).thenReturn(useRegexValue);

        boolean replaceAllValue = true;
        MetaExpression replaceAll = mock(MetaExpression.class);
        when(replaceAll.getBooleanValue()).thenReturn(replaceAllValue);

        int timeoutValue = 10;
        MetaExpression timeout = mock(MetaExpression.class);
        when(timeout.getNumberValue()).thenReturn(timeoutValue);

        Exception returnValue = new IllegalArgumentException();
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.getMatcher(needleValue, textValue, timeoutValue)).thenThrow(returnValue);

        // Run
        MetaExpression args[] = {text, needle, replacement, useRegex, replaceAll, timeout};
        ReplaceConstruct.process(args, regexService, stringService);

        // Verify
        verify(regexService, times(1)).getMatcher(needleValue, textValue, timeoutValue);
        verify(regexService, times(0)).replaceAll(any(), eq(replacementValue));
        verify(regexService, times(0)).replaceFirst(any(), eq(replacementValue));
        verify(stringService, times(0)).replaceAll(textValue, needleValue, replacementValue);
        verify(stringService, times(0)).replaceFirst(textValue, needleValue, replacementValue);
    }
}
