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
package nl.xillio.xill.plugins.excel.datastructures;

import nl.xillio.xill.api.errors.NotImplementedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the XillCell Datastructure
 *
 * @author Daan Knoope
 */
public class XillCellTest {

    /**
     * Tests if the GetValue method reads the correct cell type from the provided cell:
     * <ul>
     * <li>for cells containing strings, a string value must be returned</li>
     * <li>for cells containing booleans, a boolean value must be returned</li>
     * <li>for cells containing formulas, a string that is a direct copy of the formula must be returned</li>
     * <li>for cells containing dates, a value must be returned that can be converted to a date</li>
     * <li>for cells containing numeric values, a double must be returned</li>
     * <li>for cells that are {@code null} or {@code CELL_TYPE_BLANK}, [EMPTY] must be returned as string</li>
     * <li>for cells that are formatted in any other way, an exception must be thrown that that format is unsupported</li>
     * </ul>
     *
     * @throws Exception
     */
    @Test(expectedExceptions = NotImplementedException.class, expectedExceptionsMessageRegExp
            = "A cell format that has been used in the Excel file is currently unsupported.")
    public void testGetValue() throws Exception {
        Cell cell = mock(Cell.class);
        XillSheet sheet = mock(XillSheet.class);
        XillCell testCell = spy(new XillCell(cell, sheet));

        when(cell.getCellType()).thenReturn(cell.CELL_TYPE_STRING);
        RichTextString rich = mock(RichTextString.class);
        when(cell.getRichStringCellValue()).thenReturn(rich);
        when(rich.getString()).thenReturn("string");
        assertEquals(testCell.getValue(), "string");

        when(cell.getCellType()).thenReturn(cell.CELL_TYPE_BOOLEAN);
        when(cell.getBooleanCellValue()).thenReturn(true);
        assertTrue((boolean) testCell.getValue());

        when(cell.getCellType()).thenReturn(cell.CELL_TYPE_FORMULA);
        when(cell.getCellFormula()).thenReturn("3+3");
        assertEquals(testCell.getValue(), "3+3");

        when(cell.getCellType()).thenReturn(cell.CELL_TYPE_NUMERIC);
        doReturn(true).when(testCell).isDateFormatted();
        Date d = Date.from(Instant.now());
        when(cell.getDateCellValue()).thenReturn(d);
        ZonedDateTime result = ((DateImpl) testCell.getValue()).getZoned();
        Date resultDate = Date.from(result.toInstant());
        assertEquals(d.compareTo(resultDate), 0);

        doReturn(false).when(testCell).isDateFormatted();

        when(cell.getNumericCellValue()).thenReturn(2.5);
        when(testCell.getValue()).thenReturn(2.5);
        assertEquals(testCell.getValue(), 2.5);

        when(cell.getNumericCellValue()).thenReturn(1.0);
        when(testCell.getValue()).thenReturn(1.0);
        Object value = testCell.getValue();
        assertTrue(value instanceof Long);
        assertEquals(value, 1L); //changed from Integer to Long in CTC-1590
        
        doReturn(true).when(testCell).isNull();
        assertEquals(testCell.getValue(), null);

        when(cell.getCellType()).thenReturn(cell.CELL_TYPE_BLANK);
        doReturn(false).when(testCell).isNull();
        assertEquals(testCell.getValue(), null);

        doReturn(false).when(testCell).isNull();
        when(cell.getCellType()).thenReturn(Cell.CELL_TYPE_ERROR);
        testCell.getValue();
    }

    /**
     * Tests if the correct types are used for setting the cell's value
     */
    @Test
    public void setCellValue() throws Exception {
        Cell cell = mock(Cell.class);
        XillSheet sheet = mock(XillSheet.class);
        XillCell testCell = new XillCell(cell, sheet);

        testCell.setCellValue("1", true);
        verify(cell, times(1)).setCellFormula("1");

        testCell.setCellValue("a", false);
        verify(cell, times(1)).setCellValue("a");

        testCell.setCellValue(2d);
        verify(cell, times(1)).setCellValue(2.0);

        testCell.setCellValue(true);
        verify(cell, times(1)).setCellValue(true);

        testCell.setNull();
        verify(cell, times(1)).setCellType(Cell.CELL_TYPE_BLANK);
    }

    //It is not possible to test what happens when a FormulaParseException is thrown in
    //setCellValue(String value) since that exception (and everything that raises it) is
    //protected.

    /**
     * Tests setting the cell's value looking as a formula but being the text (in .xlsx worksheet)
     */
    @Test
    public void setCellValueXSSFText() throws Exception {
        Cell cell = mock(Cell.class);
        XillSheet sheet = mock(XillSheet.class);
        XillCell testCell = new XillCell(cell, sheet);

        XillWorkbook xillworkbook = mock(XillWorkbook.class);
        when(sheet.getParentWorkbook()).thenReturn(xillworkbook);
        XSSFWorkbook workbook = mock(XSSFWorkbook.class);
        when(xillworkbook.getWorkbook()).thenReturn(workbook);
        XSSFCellStyle style = mock(XSSFCellStyle.class);
        when(workbook.createCellStyle()).thenReturn(style);
        testCell.setCellValue("=1", false);
        verify(cell, times(1)).setCellValue("=1");
        verify(cell, times(1)).setCellStyle(any());
    }

    /**
     * Tests setting the cell's value looking as a formula but being the text (in .xls worksheet)
     */
    @Test
    public void setCellValueHSSFText() throws Exception {
        Cell cell = mock(Cell.class);
        XillSheet sheet = mock(XillSheet.class);
        XillCell testCell = new XillCell(cell, sheet);

        XillWorkbook xillworkbook = mock(XillWorkbook.class);
        when(sheet.getParentWorkbook()).thenReturn(xillworkbook);

        HSSFWorkbook workbook = new HSSFWorkbook();
        when(xillworkbook.getWorkbook()).thenReturn(workbook);

        testCell.setCellValue("=1", false);
        verify(cell, times(1)).setCellValue("=1");
        verify(cell, times(1)).setCellStyle(any());
    }

    /**
     * Tests if the text starting with equal sign is set into not supported workbook
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void setCellValueEqualSignExceptionText() throws Exception {
        Cell cell = mock(Cell.class);
        XillSheet sheet = mock(XillSheet.class);
        XillCell testCell = new XillCell(cell, sheet);

        XillWorkbook xillworkbook = mock(XillWorkbook.class);
        when(sheet.getParentWorkbook()).thenReturn(xillworkbook);
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        when(xillworkbook.getWorkbook()).thenReturn(workbook);

        testCell.setCellValue("=1", false);
    }
}
