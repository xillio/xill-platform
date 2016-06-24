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
import nl.xillio.xill.plugins.excel.datastructures.XillCellRef;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the GetCell construct.
 *
 * @author Daan Knoope
 */
public class GetCellConstructTest extends TestUtils {

    /**
     * Creates a mock for a XillSheet that will return the same for each cell in the sheet.
     *
     * @param cellContent the string which every cell should return
     * @return a {@link MetaExpression} containing the mocked {@link XillSheet}
     */
    private MetaExpression createSheetInput(String cellContent) {
        XillSheet sheet = mock(XillSheet.class);
        MetaExpression sheetInput = fromValue("Sheet");
        sheetInput.storeMeta(sheet);
        when(sheet.getCellValue(any(XillCellRef.class))).thenReturn(cellContent);
        return sheetInput;
    }

    /**
     * Checks if GetCell returns false when the cell required is null
     */
    @Test
    public void testProcessReturnsNull() throws Exception {
        MetaExpression sheetInput = createSheetInput(null);
        MetaExpression result = GetCellConstruct.process(sheetInput, fromValue(1), fromValue(1));

        assertEquals(false, result.getBooleanValue());
    }

    /**
     * Checks if GetCell reads numeric notation and returns the cell value
     */
    @Test
    public void testProcessReturnsValueNumericNotation() throws Exception {
        MetaExpression sheetInput = createSheetInput("INFO");
        MetaExpression result = GetCellConstruct.process(sheetInput, fromValue(14), fromValue(19));

        assertEquals(true, result.getBooleanValue());
    }

    /**
     * Checks if GetCell reads alphabetic notation and returns the cell value
     */
    @Test
    public void testProcessReturnsValueAlphabeticNotation() throws Exception {
        MetaExpression sheetInput = createSheetInput("INFO");
        MetaExpression result = GetCellConstruct.process(sheetInput, fromValue("AB"), fromValue(19));
        assertEquals(true, result.getBooleanValue());
    }

}
