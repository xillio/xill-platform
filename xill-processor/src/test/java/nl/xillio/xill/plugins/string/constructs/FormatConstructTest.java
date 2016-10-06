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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.string.exceptions.FailedToGetMatcherException;
import nl.xillio.xill.plugins.string.services.string.RegexService;
import nl.xillio.xill.plugins.string.services.string.StringUtilityService;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link FormatConstruct}.
 */
public class FormatConstructTest extends TestUtils{

    /**
     * Test the process method under normal circumstances.
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    // @Test
    public void processNormalUsage() throws IOException {
        // TODO
    }

    /**
     * <p>
     * Tests wheter the process can handle a syntax error in the pattern given to the matcher
     * </p>
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "SyntaxError in the system provided pattern: .*")
    public void processPatternSyntaxException() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String fileNameValue = "decimal %d%n";
        MetaExpression fileName = mockExpression(ATOMIC);
        when(fileName.getStringValue()).thenReturn(fileNameValue);
        when(fileName.isNull()).thenReturn(false);

        ArrayList<MetaExpression> listValue = new ArrayList<>();
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);

        Exception exception = new PatternSyntaxException("", "", 0);
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.getMatcher(anyString(), anyString(), anyInt())).thenThrow(exception);

        FormatConstruct construct = new FormatConstruct(regexService, stringService);
        // Run
        process(construct, fileName, list);

        // Verify
        verify(regexService, times(1)).getMatcher(anyString(), anyString(), anyInt());
        verify(regexService, times(0)).tryMatch(any());
        verify(stringService, times(0)).format(anyString(), any());
    }

    /**
     * <p>
     * Tests wheter the process can handle an illegal argument given to the matcher.
     * </p>
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Illegal argument handed when trying to match: .*")
    public void processIllegalArgumentException() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String fileNameValue = "decimal %d%n";
        MetaExpression fileName = mockExpression(ATOMIC);
        when(fileName.getStringValue()).thenReturn(fileNameValue);
        when(fileName.isNull()).thenReturn(false);

        ArrayList<MetaExpression> listValue = new ArrayList<>();
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);

        Exception exception = new IllegalArgumentException();
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.getMatcher(anyString(), anyString(), anyInt())).thenThrow(exception);

        FormatConstruct construct = new FormatConstruct(regexService, stringService);
        // Run
        process(construct, fileName, list);

        // Verify
        verify(regexService, times(1)).getMatcher(anyString(), anyString(), anyInt());
        verify(regexService, times(0)).tryMatch(any());
        verify(stringService, times(0)).format(anyString(), any());
    }

    /**
     * <p>
     * Tests wheter the process can handle an illegal argument given to the matcher.
     * </p>
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = "Could not format a date/time..*Date/Time conversions are not supported..*")
    public void processDateTimeConversion() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String fileNameValue = "decimal %T%n";
        MetaExpression fileName = mockExpression(ATOMIC);
        when(fileName.getStringValue()).thenReturn(fileNameValue);
        when(fileName.isNull()).thenReturn(false);

        List<MetaExpression> listValue = new ArrayList<>();
        MetaExpression listItem = mockExpression(ATOMIC);
        listValue.add(listItem);
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);

        String errorValue = "T";
        List<String> matchValue = Arrays.asList(errorValue);
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.tryMatch(any())).thenReturn(matchValue);

        FormatConstruct construct = new FormatConstruct(regexService, stringService);
        // Run
        process(construct, fileName, list);

        // Verify
        verify(regexService, times(1)).getMatcher(anyString(), anyString(), anyInt());
        verify(regexService, times(1)).tryMatch(any());
        verify(stringService, times(0)).format(anyString(), any());
    }

    /**
     * <p>
     * Tests wheter the process can handle an illegal argument given to the matcher.
     * </p>
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Unexpected conversion type..*Z.*")
    public void processUnexpectedConversion() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String fileNameValue = "decimal %Z%n";
        MetaExpression fileName = mockExpression(ATOMIC);
        when(fileName.getStringValue()).thenReturn(fileNameValue);
        when(fileName.isNull()).thenReturn(false);

        List<MetaExpression> listValue = new ArrayList<>();
        MetaExpression listItem = mockExpression(ATOMIC);
        listValue.add(listItem);
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);

        String errorValue = "Z";
        List<String> matchValue = Arrays.asList(errorValue);
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.tryMatch(any())).thenReturn(matchValue);

        FormatConstruct construct = new FormatConstruct(regexService, stringService);
        // Run
        process(construct, fileName, list);

        // Verify
        verify(regexService, times(1)).getMatcher(anyString(), anyString(), anyInt());
        verify(regexService, times(1)).tryMatch(any());
        verify(stringService, times(0)).format(anyString(), any());
    }

    /**
     * <p>
     * Tests wheter the process can handle an illegal argument given to the matcher.
     * </p>
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Not enough arguments: .*")
    public void processMissingFormatException() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String textValue = "decimal";
        MetaExpression text = mockExpression(ATOMIC);
        when(text.getStringValue()).thenReturn(textValue);
        when(text.isNull()).thenReturn(false);

        List<MetaExpression> listValue = new ArrayList<>();
        MetaExpression listItem = mockExpression(ATOMIC);
        listValue.add(listItem);
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);

        List<String> matchValue = Arrays.asList();
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.tryMatch(any())).thenReturn(matchValue);
        when(stringService.format(eq(textValue), any())).thenThrow(new MissingFormatArgumentException("argument"));

        FormatConstruct construct = new FormatConstruct(regexService, stringService);
        // Run
        process(construct, text, list);

        // Verify
        verify(regexService, times(1)).getMatcher(anyString(), anyString(), anyInt());
        verify(regexService, times(1)).tryMatch(any());
        verify(stringService, times(1)).format(anyString(), any());
    }

    /**
     * <p>
     * Tests wheter the process can handle an illegal argument given to the matcher.
     * </p>
     *
     * @throws FailedToGetMatcherException
     * @throws IllegalArgumentException
     * @throws PatternSyntaxException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Illegal format handed: .*")
    public void processIllegalFormatException() throws IllegalArgumentException, FailedToGetMatcherException {
        // Mock
        String textValue = "decimal";
        MetaExpression text = mockExpression(ATOMIC);
        when(text.getStringValue()).thenReturn(textValue);
        when(text.isNull()).thenReturn(false);

        List<MetaExpression> listValue = new ArrayList<>();
        MetaExpression listItem = mockExpression(ATOMIC);
        listValue.add(listItem);
        MetaExpression list = mockExpression(LIST);
        when(list.getValue()).thenReturn(listValue);

        List<String> matchValue = Arrays.asList();
        RegexService regexService = mock(RegexService.class);
        StringUtilityService stringService = mock(StringUtilityService.class);
        when(regexService.tryMatch(any())).thenReturn(matchValue);
        when(stringService.format(eq(textValue), any())).thenThrow(new IllegalFormatPrecisionException(3));

        FormatConstruct construct = new FormatConstruct(regexService, stringService);
        // Run
        process(construct, text, list);

        // Verify
        verify(regexService, times(1)).getMatcher(anyString(), anyString(), anyInt());
        verify(regexService, times(1)).tryMatch(any());
        verify(stringService, times(1)).format(anyString(), any());
    }
}
