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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the columnCount construct.
 *
 * @author Daan Knoope
 */
public class ColumnCountConstructTest {

    /**
     * Tests if the result of the construct contains the right number
     *
     * @throws Exception
     */
    @Test
    public void testProcess() throws Exception {
        XillSheet sheet = mock(XillSheet.class);
        when(sheet.getColumnLength()).thenReturn(11);
        LinkedHashMap<String, MetaExpression> sheetObject = new LinkedHashMap<>();
        MetaExpression sheetInput = fromValue(sheetObject);
        sheetInput.storeMeta(sheet);
        MetaExpression result = ColumnCountConstruct.process(sheetInput);
        assertEquals(11, result.getNumberValue().intValue());
    }

    /**
     * Tests if a RobotRuntimeException is thrown when no valid sheet is supplied
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected parameter 'sheet' to be a result of loadSheet or createSheet")
    public void testProcessThrowsException() throws Exception {
        LinkedHashMap<String, MetaExpression> sheetObject = new LinkedHashMap<>();
        MetaExpression sheetInput = fromValue(sheetObject);
        sheetInput.storeMeta(null);
        ColumnCountConstruct.process(fromValue(sheetObject));
    }
}
