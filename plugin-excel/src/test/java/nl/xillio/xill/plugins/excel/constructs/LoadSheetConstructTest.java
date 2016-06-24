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
import nl.xillio.xill.plugins.excel.NoSuchSheetException;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the LoadSheet construct
 *
 * @author Daan Knoope
 */
public class LoadSheetConstructTest extends TestUtils {

    /**
     * Checks if a RobotRuntimeException is thrown when no valid workbook is supplied.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Expected parameter 'workbook' to be a result of loadWorkbook or createWorkbook")
    public void testProcessNoWorkbook() throws Exception {
        MetaExpression input = fromValue("workbook object");

        LoadSheetConstruct.process(input, fromValue("Sheet"));
    }

    /**
     * Checks if a RobotRuntimeException is thrown when the name of the sheet cannot be
     * found in the provided workbook.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessThrowsRobotRuntime() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression input = fromValue("workbook object");
        input.storeMeta(workbook);

        when(workbook.getSheet("sheet")).thenThrow(new NoSuchSheetException(""));

        LoadSheetConstruct.process(input, fromValue("sheet"));
    }

    /**
     * Checks if the LoadSheet construct returns a
     * <ul>
     * <li>correctly formatted string, containing the sheet name, amount of rows and amount of columns</li>
     * <li>the same XillSheet in the MetaExpression as the one it created.</li>
     * </ul>
     */
    @Test
    public void testProcessReturnsCorrectly() throws Exception {

        //Create basic vars
        XillWorkbook workbook = mock(XillWorkbook.class);
        XillSheet sheet = mock(XillSheet.class);
        MetaExpression input = fromValue("workbook object");
        input.storeMeta(workbook);

        //Mock sheet object
        when(workbook.getSheet(anyString())).thenReturn(sheet);
        when(sheet.getName()).thenReturn("SheetName");
        when(sheet.getRowLength()).thenReturn(3);
        when(sheet.getColumnLength()).thenReturn(5);

        //Get result
        MetaExpression result = LoadSheetConstruct.process(input, fromValue("Sheet"));

        //Check results
        assertEquals(result.getStringValue(), "{\"sheetName\":\"SheetName\",\"rows\":3,\"columns\":5}");
        XillSheet resultSheet = result.getMeta(XillSheet.class);
        assertEquals(resultSheet, sheet);
    }
}
