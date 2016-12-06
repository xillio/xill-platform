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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Unit tests for the XillSheet data structure
 *
 * @author Daan Knoope
 */
public class XillSheetTest {

    /**
     * Tests if the width of a sheet is correctly calculated
     */
    @Test
    public void testGetColumnLength() throws Exception {
        Sheet sheet = mock(Sheet.class);
        when(sheet.getLastRowNum()).thenReturn(1);

        Row row1 = mock(Row.class);
        Row row2 = mock(Row.class);

        when(sheet.getRow(0)).thenReturn(row1);
        when(sheet.getRow(1)).thenReturn(row2);

        when(row1.getLastCellNum()).thenReturn((short) 82);
        when(row2.getLastCellNum()).thenReturn((short) 81);

        XillSheet testSheet = new XillSheet(sheet, false, mock(XillWorkbook.class));
        assertEquals(82, testSheet.getColumnLength());
    }

    /**
     * Tests if the correct number of rows is returned
     */
    @Test
    public void testGetRowLength() throws Exception {
        Sheet sheet = mock(Sheet.class);

        when(sheet.getLastRowNum()).thenReturn(3);
        XillSheet testSheet = new XillSheet(sheet, false, mock(XillWorkbook.class));

        assertEquals(4, testSheet.getRowLength());
    }

    /**
     * Tests if the correct name of a sheet is returned
     */
    @Test
    public void testGetName() throws Exception {
        Sheet sheet = mock(Sheet.class);
        when(sheet.getSheetName()).thenReturn("sheet");
        XillSheet testsheet = new XillSheet(sheet, false, mock(XillWorkbook.class));
        assertEquals("sheet", testsheet.getName());
    }

    /**
     * Tests that the GetCellValue method returns the correct value from a cell
     */
    @Test
    public void testGetCellValue() throws Exception {
        XillRow row = mock(XillRow.class);
        Sheet sheet = mock(Sheet.class);
        XillSheet xillSheet = spy(new XillSheet(sheet, false, mock(XillWorkbook.class)));
        XillCell cell = mock(XillCell.class);

        doReturn(row).when(xillSheet).getRow(any(Integer.class));
        when(cell.getValue()).thenReturn(true);
        when(row.getCell(any(Integer.class), any(XillSheet.class))).thenReturn(cell);

        assertTrue((boolean) xillSheet.getCellValue(new XillCellRef(1, 1)));
    }

    /**
     * Tests that "[EMPTY]" is returned when a row (where the cell should be in) is null
     */
    @Test
    public void testGetCellValueEmpty() throws Exception {
        Sheet sheet = mock(Sheet.class);
        XillSheet xillSheet = spy(new XillSheet(sheet, false, mock(XillWorkbook.class)));
        XillRow row = mock(XillRow.class);

        doReturn(row).when(xillSheet).getRow(any(Integer.class));
        when(row.isNull()).thenReturn(true);

        assertEquals(null, xillSheet.getCellValue(new XillCellRef(1, 1)));
    }

    /**
     * Tests that the correct value is returned when a cell is called by a XillCellRef
     */
    @Test
    public void getCell() throws Exception {
        XillCellRef cellRef = new XillCellRef(3, 3);
        XillSheet sheet = spy(new XillSheet(mock(Sheet.class), false, mock(XillWorkbook.class)));
        XillRow row = mock(XillRow.class);

        doReturn(row).when(sheet).getRow(cellRef.getRow());
        when(row.isNull()).thenReturn(false);
        XillCell cell = mock(XillCell.class);
        when(row.getCell(cellRef.getRow(), sheet)).thenReturn(cell);
        when(cell.isNull()).thenReturn(false);

        assertTrue(sheet.getCell(cellRef).equals(cell));
    }

    /**
     * Tests that a new cell is created when it is required and does not exist yet
     */
    @Test
    public void getCellDoesNotExist() throws Exception {
        XillCellRef cellRef = new XillCellRef(4, 3);
        Sheet sheet = mock(Sheet.class);
        XillSheet testSheet = spy(new XillSheet(sheet, false, mock(XillWorkbook.class)));

        XillRow emptyRow = mock(XillRow.class);
        when(emptyRow.isNull()).thenReturn(true);

        XillRow newRow = mock(XillRow.class);
        XillCell cell = mock(XillCell.class);
        when(newRow.getCell(cellRef.getColumn(), testSheet)).thenReturn(cell);
        when(cell.isNull()).thenReturn(true);
        when(newRow.createCell(cellRef.getColumn(), testSheet)).thenReturn(cell);

        doReturn(emptyRow).when(testSheet).getRow(cellRef.getRow());
        doReturn(newRow).when(testSheet).createRow(cellRef.getRow());

        assertTrue(cell.equals(testSheet.getCell(cellRef)));
    }

    /**
     * Tests that a cell can be overwritten and that all three of the
     * signatures work for SetCellValue
     */
    @Test
    public void testSetCellValue() throws Exception {
        XillCellRef cellRef = new XillCellRef(5, 3);
        String value = "val";
        Sheet sheet = mock(Sheet.class);
        XillSheet testSheet = spy(new XillSheet(sheet, false, mock(XillWorkbook.class)));
        XillCell cell = mock(XillCell.class);
        doReturn(cell).when(testSheet).getCell(any(XillCellRef.class));

        testSheet.setCellValue(cellRef, value, false);
        testSheet.setCellValue(cellRef, 3d);
        testSheet.setCellValue(cellRef, false);

        verify(testSheet, times(3)).getCell(cellRef);
    }

    /**
     * Tests that a sheet returns if it is read-only
     */
    @Test
    public void testIsReadonly() throws Exception {
        Sheet sheet = mock(Sheet.class);
        XillSheet testSheet = new XillSheet(sheet, true, mock(XillWorkbook.class));
        assertTrue(testSheet.isReadonly());
        testSheet = new XillSheet(sheet, false, mock(XillWorkbook.class));
        assertFalse(testSheet.isReadonly());
    }

    /**
     * Tests if setCellValue enlarges the sheet when a cell outside its bounds
     * has been created.
     *
     * @throws Exception
     */
    @Test
    public void testSetCellValueEnlargesColumn() throws Exception {
        //Creating setup
        Sheet oldsheet = mock(Sheet.class);
        when(oldsheet.getLastRowNum()).thenReturn(4);
        XillSheet sheet = spy(new XillSheet(oldsheet, false, mock(XillWorkbook.class)));
        XillCellRef cellRef = mock(XillCellRef.class);
        doReturn(mock(XillCell.class)).when(sheet).getCell(cellRef);
        doReturn(3).when(sheet).calculateColumnLength();

        //Testing boolean signature
        when(cellRef.getColumn()).thenReturn(10);
        sheet.setCellValue(cellRef, false);
        assertEquals(11, sheet.getColumnLength());

        //Testing string signature
        when(cellRef.getColumn()).thenReturn(11);
        sheet.setCellValue(cellRef, "string", false);
        assertEquals(12, sheet.getColumnLength());

        //Testing double signature
        when(cellRef.getColumn()).thenReturn(12);
        sheet.setCellValue(cellRef, 1.3);
        assertEquals(13, sheet.getColumnLength());

        //Verifying that the proper method has been called
        verify(cellRef, times(6)).getColumn();
    }

    /**
     * Test {@link XillSheet#forceFormulaRecalculation()}
     */
    @Test
    public void testForceFormulaRecalculation(){
        // Mock
        Sheet sheet = mock(Sheet.class);
        XillWorkbook xillWorkbook = mock(XillWorkbook.class);
        XillSheet xillSheet = new XillSheet(sheet, false, xillWorkbook);

        // Run
        xillSheet.forceFormulaRecalculation();

        // Verify
        verify(sheet).setForceFormulaRecalculation(true);
    }

}
