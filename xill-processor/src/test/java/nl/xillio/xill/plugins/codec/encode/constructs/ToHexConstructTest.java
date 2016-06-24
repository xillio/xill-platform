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
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.codec.encode.services.EncoderServiceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Full test of ToHexConstruct.
 *
 * @author Paul van der Zandt
 * @since 3.0
 */
public class ToHexConstructTest extends TestUtils {

    private ConstructProcessor processor;

    @BeforeMethod
    public void initTest() {
        ConstructContext context = mock(ConstructContext.class);
        ToHexConstruct construct = new ToHexConstruct(new EncoderServiceImpl());
        processor = construct.prepareProcess(context);
    }

    @Test
    public void testPrepareProcess() throws Exception {
        processor.setArgument(0, fromValue("ABC"));
        MetaExpression result = processor.process();
        assertEquals(result.getStringValue(), "414243");
    }

    @Test
    public void testPrepareProcessDiacritics() throws Exception {
        processor.setArgument(0, fromValue("äëÄ"));
        MetaExpression result = processor.process();
        assertEquals(result.getStringValue(), "C3A4C3ABC384");
    }

    @Test
    public void testPrepareProcessDiacriticsCharset() throws Exception {
        processor.setArgument(0, fromValue("äëÄ"));
        processor.setArgument(1, fromValue(true));
        processor.setArgument(2, fromValue("ISO-8859-1"));
        MetaExpression result = processor.process();
        assertEquals(result.getStringValue(), "e4ebc4");
    }

    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Unknown character set..*DOES-NOT-EXIST.*")
    public void testPrepareProcessInvalidCharset() throws Exception {
        processor.setArgument(0, fromValue("äëÄ"));
        processor.setArgument(1, fromValue(true));
        processor.setArgument(2, fromValue("DOES-NOT-EXIST"));
        processor.process();
        fail();
    }

    @Test
    public void testPrepareProcessNull() throws Exception {
        processor.setArgument(0, NULL);
        MetaExpression expression = processor.process();
        assertTrue(expression.isNull());
    }

}