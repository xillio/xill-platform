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
package nl.xillio.xill.plugins.excel.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests for the utility methods of AbstractExcelConstruct.
 *
 * @author Xavier Pardonnet
 */
public class AbstractExcelConstructTest extends TestUtils {

    @Test
    public void testAssertNumericValid() throws Exception {
        MetaExpression parameter = fromValue(1);
        assertEquals(AbstractExcelConstruct.assertNumeric(parameter, "parameter"), 1);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testAssertNumericInvalid() throws Exception {
        MetaExpression parameter = fromValue("a");
        AbstractExcelConstruct.assertNumeric(parameter, "parameter");
    }

    @Test
    public void testAssertAlphaValid() throws Exception {
        MetaExpression parameter = fromValue("A");
        assertEquals(AbstractExcelConstruct.assertAlpha(parameter, "parameter"), "A");
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testAssertAlphaInValid() throws Exception {
        MetaExpression parameter = fromValue("A0");
        AbstractExcelConstruct.assertAlpha(parameter, "parameter");
    }

    @Test
    public void testIsNumericYes() throws Exception {
        MetaExpression parameter = fromValue(1);
        assertTrue(AbstractExcelConstruct.isNumeric(parameter));
    }

    @Test
    public void testIsNumericNo() throws Exception {
        MetaExpression parameter = fromValue("A");
        assertFalse(AbstractExcelConstruct.isNumeric(parameter));
    }
}