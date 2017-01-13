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

import nl.xillio.xill.plugins.excel.NoSuchSheetException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the XillWorkbook datastructure
 *
 * @author Daan Knoope
 */
public class XillWorkbookTest {

    /**
     * Creates a mocked File
     *
     * @param path     the (fake) path the mocked File should point to
     * @param readonly if the path is readonly
     * @return the mocked File
     */
    public File createFile(String path, boolean readonly) throws Exception {
        File file = mock(File.class);
        when(file.getCanonicalPath()).thenReturn(path);
        when(file.canWrite()).thenReturn(!readonly);
        when(file.getParentFile()).thenReturn(file);
        return file;
    }

    /**
     * Tests if an Exception is thrown when a sheet is
     * asked for that does not exist in this workbook
     */
    @Test(expectedExceptions = NoSuchSheetException.class)
    public void testGetSheetThatDoesNotExist() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        when(workbook.getSheetIndex(anyString())).thenReturn(-1);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        testWorkbook.getSheet("sheet");
    }

    /**
     * Tests if an Exception is thrown when a sheet is
     * asked for that does not exist in this workbook
     */
    @Test(expectedExceptions = NoSuchSheetException.class)
    public void testGetSheetNumSmallerThanZero() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        testWorkbook.getSheetAt(-1);
    }

    /**
     * Tests if an Exception is thrown when a sheet is
     * asked for that does not exist in this workbook
     */
    @Test(expectedExceptions = NoSuchSheetException.class)
    public void testGetSheetNumThatDoesNotExist() throws Exception {
        Workbook workbook = mock(Workbook.class);
        when(workbook.getNumberOfSheets()).thenReturn(1);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        testWorkbook.getSheetAt(5);
    }

    /**
     * Tests if a sheet is correctly returned and
     * if the string that appears in the debugger when a workbook is loaded
     * is correct.
     */
    @Test
    public void testGetSheetandWorkbookName() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        when(workbook.getSheetIndex(anyString())).thenReturn(2);
        Sheet sheet = mock(Sheet.class);
        when(workbook.getSheet("sheet")).thenReturn(sheet);
        when(sheet.getSheetName()).thenReturn("sheet");
        assertEquals("sheet", testWorkbook.getSheet("sheet").getName());
        assertEquals("Excel Workbook [path]", testWorkbook.getFileString());
    }

    /**
     * Tests if a sheet is correctly returned and
     * if the string that appears in the debugger when a workbook is loaded
     * is correct.
     */
    @Test
    public void testGetSheetNumAndWorkbookName() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        when(workbook.getNumberOfSheets()).thenReturn(1);
        Sheet sheet = mock(Sheet.class);
        when(workbook.getSheetAt(0)).thenReturn(sheet);
        when(sheet.getSheetName()).thenReturn("sheet");
        assertEquals("sheet", testWorkbook.getSheetAt(0).getName());
        assertEquals("Excel Workbook [path]", testWorkbook.getFileString());
    }

    /**
     * Verifies that when a new sheet is made, the correct method is
     * called and the name stays the same throughout the process.
     *
     * @throws Exception
     */
    @Test
    public void testMakeSheet() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        Sheet sheet = mock(Sheet.class);
        when(workbook.createSheet("name")).thenReturn(sheet);
        when(sheet.getSheetName()).thenReturn("name");
        XillSheet result = testWorkbook.makeSheet("name");

        verify(workbook, times(1)).createSheet("name");
        assertEquals(result.getName(), "name");
    }

    /**
     * Tests that when no sheets are in a workbook, an empty list is returned when
     * queried for the name of the sheets.
     */
    @Test
    public void testGetSheetNamesEmpty() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        when(workbook.getNumberOfSheets()).thenReturn(0);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        assertTrue(testWorkbook.getSheetNames().isEmpty());
    }

    /**
     * Tests that getSheetNames fetches all the names of the sheets in the workbook
     */
    @Test
    public void testGetSheetNames() throws Exception {
        List<String> sheetNames = Arrays.asList("Apple", "Bee", "Cow");

        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        when(workbook.getNumberOfSheets()).thenReturn(sheetNames.size());

        Sheet[] sheets = new Sheet[3];
        for (int i = 0; i < sheets.length; i++) {
            sheets[i] = mock(Sheet.class);
            when(sheets[i].getSheetName()).thenReturn(sheetNames.get(i));
            when(workbook.getSheetAt(i)).thenReturn(sheets[i]);
        }
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        assertTrue(testWorkbook.getSheetNames().equals(sheetNames));
    }

    /**
     * Tests that when a file is read-only, the workbook also gets the
     * read-only property set to {@code true}
     */
    @Test
    public void testIsReadonly() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", true);
        when(file.exists()).thenReturn(true);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        assertTrue(testWorkbook.isReadOnly());
    }

    /**
     * Tests that the location of the workbook is correct
     *
     * @throws Exception
     */
    @Test
    public void testGetLocation() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        assertEquals("path", testWorkbook.getLocation());
    }

    /**
     * Tests that when a sheet should be removed, the correct method
     * to continue the operation is called and that the sheet is
     * removed.
     */
    @Test
    public void testRemoveSheet() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        when(workbook.getSheetIndex("sheet")).thenReturn(3);
        testWorkbook.removeSheet("sheet");
        verify(workbook, times(1)).removeSheetAt(3);
    }

    /**
     * Tests that when a sheet name does not exist, an IllegalArgumentException is thrown
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Sheet sheet does not exist in this workbook")
    public void testRemoveSheetDoesNotExist() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = new XillWorkbook(workbook, file);
        when(workbook.getSheetIndex(anyString())).thenReturn(-1);
        testWorkbook.removeSheet("sheet");
    }

    /**
     * Tests that no exception is thrown when Save is used with the correct object
     */
    @Test
    public void testSave() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = spy(new XillWorkbook(workbook, file));
        FileOutputStream stream = mock(FileOutputStream.class);
        doReturn(stream).when(testWorkbook).getOutputStream(file);
        testWorkbook.save();
        testWorkbook.save(file);
    }

    /**
     * Tests that an exception is thrown when a workbook tried to save
     * (by overwriting), but the I/O operation did not succeed
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IOException.class)
    public void testSaveException() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = spy(new XillWorkbook(workbook, file));
        doReturn(mock(FileOutputStream.class)).when(testWorkbook).getOutputStream(any(File.class));
        doThrow(new IOException()).when(workbook).write(any(OutputStream.class));
        testWorkbook.save();
    }

    /**
     * Tests that an exception is thrown when a workbook can not be saved to
     * the new path specified.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IOException.class)
    public void testSaveWithFileException() throws Exception {
        Workbook workbook = mock(Workbook.class);
        File file = createFile("path", false);
        XillWorkbook testWorkbook = spy(new XillWorkbook(workbook, file));
        FileOutputStream stream = mock(FileOutputStream.class);
        doReturn(stream).when(testWorkbook).getOutputStream(file);
        doThrow(new IOException()).when(workbook).write(any(OutputStream.class));
        testWorkbook.save(file);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "New file should have the same extension as original \\(xls, not xlsx\\)")
    public void testCreateCopyExtensionMismatch() throws Exception {
        File file = mock(File.class);
        XillWorkbook workbook = new XillWorkbook(new HSSFWorkbook(), mock(File.class));
        when(file.getName()).thenReturn("name.xlsx");
        workbook.createCopy(file);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "New file should have the same extension as original \\(xlsx, not xls\\)")
    public void testCreateCopyExtensionMismatch2() throws Exception {
        File file = mock(File.class);
        XillWorkbook workbook = new XillWorkbook(new XSSFWorkbook(), mock(File.class));
        when(file.getName()).thenReturn("name.xls");
        workbook.createCopy(file);
    }

    @Test
    public void testCreateCopy() throws Exception {
        // Mock
        File file = mock(File.class);
        XillWorkbook workbook = spy(new XillWorkbook(new XSSFWorkbook(), file));

        File returnFile = mock(File.class);
        when(returnFile.getName()).thenReturn("name2.xlsx");
        when(returnFile.getParentFile()).thenReturn(mock(File.class));

        doNothing().when(workbook).copy(file, returnFile);
        doNothing().when(workbook).save(returnFile);

        XillWorkbookFactory factory = mock(XillWorkbookFactory.class);
        XillWorkbook returnbook = mock(XillWorkbook.class);
        when(factory.loadWorkbook(returnFile)).thenReturn(returnbook);

        doReturn(factory).when(workbook).getFactory();

        // Run
        XillWorkbook result = workbook.createCopy(returnFile);

        // Verify
        verify(returnFile, times(1)).setWritable(true);
        verify(workbook).save(returnFile);
        assertEquals(result, returnbook);
    }

}
