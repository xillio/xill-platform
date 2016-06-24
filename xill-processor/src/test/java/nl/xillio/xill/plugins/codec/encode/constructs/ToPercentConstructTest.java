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
package nl.xillio.xill.plugins.codec.encode.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.codec.encode.services.EncoderService;
import nl.xillio.xill.plugins.codec.encode.services.EncoderServiceImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the {@link ToPercentConstruct}.
 */
public class ToPercentConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances.
     */
    @Test
    public void processNormalUsage() throws UnsupportedEncodingException {
        // Mock
        String inputValue = "this+-is / test";
        String resultValue = "this%2B-is%20%2F%20test";

        MetaExpression textVar = fromValue(inputValue);
        MetaExpression xWwwFormVar = NULL;

        EncoderService stringService = new EncoderServiceImpl();
        ToPercentConstruct construct = new ToPercentConstruct(stringService);

        // Run
        MetaExpression result = construct.process(textVar, xWwwFormVar);

        // Assert
        Assert.assertEquals(result.getStringValue(), resultValue);
    }

    /**
     * Test the process method when URL encoding fails.
     */
    @Test(expectedExceptions = OperationFailedException.class, expectedExceptionsMessageRegExp = "Could not URL encode the string..*")
    public void processUnsupportedEncodingException() throws UnsupportedEncodingException {
        // Mock
        String inputValue = "this+-is / test";
        String resultValue = "this%2B-is%20%2F%20test";

        MetaExpression textVar = mock(MetaExpression.class);
        when(textVar.isNull()).thenReturn(false);
        when(textVar.getStringValue()).thenReturn(inputValue);

        MetaExpression xWwwFormVar = mock(MetaExpression.class);
        when(xWwwFormVar.isNull()).thenReturn(true);

        EncoderService stringService = mock(EncoderService.class);
        when(stringService.urlEncode(inputValue, false)).thenThrow(new UnsupportedEncodingException());
        ToPercentConstruct construct = new ToPercentConstruct(stringService);
        // Run
        construct.process(textVar, xWwwFormVar);
    }
}
