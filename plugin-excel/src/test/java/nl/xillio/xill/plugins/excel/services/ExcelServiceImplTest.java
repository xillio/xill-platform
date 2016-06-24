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
package nl.xillio.xill.plugins.excel.services;

import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbookFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the ExcelService implementation
 *
 * @author Daan Knoope
 */
public class ExcelServiceImplTest {

    /**
     * Creates a new ExcelServiceImplementation
     *
     * @param factory the factory which should be passed to the service implementation (can be null)
     * @return the (non-mocked, actual) service
     */
    private ExcelService createService(XillWorkbookFactory factory) {
        ExcelService service = new ExcelServiceImpl(factory);
        return service;
    }

    /**
     * Creates a mocked {@link File}
     *
     * @param exists           boolean if the file exists
     * @param correctExtension boolean if the file has a correct extension
     * @return the mocked {@link File}
     */
    private File createFile(boolean exists, boolean correctExtension) throws IOException {
        File file = mock(File.class);
        when(file.exists()).thenReturn(exists);

        if (correctExtension)
            when(file.getCanonicalPath()).thenReturn("abc.xls");
        else
            when(file.getCanonicalPath()).thenReturn("xls.abc");

        return file;
    }

    /**
     * Creates a mocked workbook
     *
     * @param isReadOnly boolean to describe if the workbook should be read-only
     * @return the mocked {@link XillWorkbook}
     */
    private XillWorkbook createWorkbook(boolean isReadOnly) {
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(isReadOnly);
        return workbook;
    }

    /**
     * Checks if loadWorkbook throws an exception when there is no file at the given path
     *
     * @throws Exception
     */
    @Test(expectedExceptions = FileNotFoundException.class,
            expectedExceptionsMessageRegExp = "There is no file at the given path")
    public void testLoadWorkbookNoFileExists() throws Exception {
        File file = createFile(false, true);
        createService(null).loadWorkbook(file);
    }

    /**
     * Tests if loadWorkbook throws an exception when the extension is incorrect
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Path does not lead to an xls or xlsx Microsoft Excel file")
    public void testLoadWorkbookIncorrectExtension() throws Exception {
        File file = createFile(true, false);
        ExcelService service = createService(null);
        service.loadWorkbook(file);
    }

    /**
     * Tests if loadWorkbook returns the workbook correctly
     *
     * @throws Exception
     */
    @Test
    public void loadWorkbookReturnsWorkbook() throws Exception {
        File file = createFile(true, true);

        XillWorkbookFactory factory = mock(XillWorkbookFactory.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(factory.loadWorkbook(any(File.class))).thenReturn(workbook);

        ExcelService service = createService(factory);

        assertEquals(service.loadWorkbook(file), workbook);
    }

    /**
     * Tests if createWorkbook returns the correct new workbook
     *
     * @throws Exception
     */
    @Test
    public void createWorkbookReturnsWorkbook() throws Exception {
        File file = createFile(false, true);

        XillWorkbookFactory factory = mock(XillWorkbookFactory.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(factory.createWorkbook(any(File.class))).thenReturn(workbook);

        ExcelService service = createService(factory);
        assertEquals(service.createWorkbook(file), workbook);
    }

    /**
     * Tests if a null pointer exception is thrown when no valid XillWorkbook was provided to createSheet
     *
     * @throws Exception
     */
    @Test(expectedExceptions = NullPointerException.class,
            expectedExceptionsMessageRegExp = "The provided workbook is invalid")
    public void testCreateSheetWorkbookNull() throws Exception {
        ExcelService service = createService(null);
        service.createSheet(null, "");
    }

    /**
     * Tests if an IllegalArgumentException is thrown when an empty string is passed to CreateSheet as name
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "No name was supplied: sheet names must be at least one character long")
    public void testCreateSheetNoName() throws Exception {
        ExcelService service = createService(null);
        service.createSheet(createWorkbook(false), "");
    }

    /**
     * Tests if an IllegalArgumentException is thrown when a null value is passed to CreateSheet as name
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "No name was supplied: sheet names must be at least one character long")
    public void testCreateSheetNameNull() throws Exception {
        ExcelService service = createService(null);
        service.createSheet(createWorkbook(false), null);
    }

    /**
     * Tests if an IllegalArgument exception is thrown when the name of the sheet consists out of too many characters
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Sheet name is too long: must be less than 32 characters")
    public void testCreateSheetNameTooLong() throws Exception {
        ExcelService service = createService(null);
        service.createSheet(createWorkbook(false), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); //Exactly 32 a's
    }

    /**
     * Tests if an IllegalArgumentException is thrown when CreateSheet was used on a read-only workbook
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Workbook is read-only")
    public void testCreateSheetReadOnly() throws Exception {
        ExcelService service = createService(null);
        service.createSheet(createWorkbook(true), "a");
    }

    /**
     * Tests if a correct sheet is returned by CreateSheet
     *
     * @throws Exception
     */
    @Test
    public void testCreateSheet() throws Exception {
        ExcelService service = createService(null);
        XillWorkbook workbook = createWorkbook(false);
        XillSheet sheet = mock(XillSheet.class);
        when(workbook.makeSheet(anyString())).thenReturn(sheet);
        assertEquals(sheet, service.createSheet(workbook, "bla"));
    }

    /**
     * Tests if the correct method is called to delete a sheet when RemoveSheet is called
     *
     * @throws Exception
     */
    @Test
    public void testRemovesheet() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        ExcelServiceImpl service = new ExcelServiceImpl(null);
        when(workbook.isReadOnly()).thenReturn(false);
        when(workbook.fileExists()).thenReturn(true);
        service.removeSheet(workbook, "name");
        verify(workbook, times(1)).removeSheet(anyString());
    }

    /**
     * Tests if {@link ExcelServiceImpl#notInWorkbook(XillWorkbook, List)} correctly creates a string
     * containing the information about sheets that are in the list to be deleted, but do not exist in the workbook
     * (and can thus not be deleted)
     *
     * @throws Exception
     */
    @Test
    public void testNotInWorkbook() throws Exception {
        ExcelServiceImpl service = new ExcelServiceImpl(null);

        //Creating a workbook, containing the sheets "foo" and "bar
        List<String> existingSheetNames = Arrays.asList("foo", "bar");
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.getSheetNames()).thenReturn(existingSheetNames);

        //Test lists of sheet names
        List<String> notInWorkbook = Arrays.asList("Oak", "Willow", "Maple");
        List<String> partiallyInWorkbook = Arrays.asList("foo", "Oak");
        List<String> singletonInWorbook = Arrays.asList("foo");
        List<String> exactlyInWorkbook = Arrays.asList("foo", "bar");

        //Verify the correct strings are generated
        assertEquals(service.notInWorkbook(workbook, notInWorkbook), "Sheet(s) [Oak,Willow,Maple] do not exist in the current workbook; they could not be deleted.");
        assertEquals(service.notInWorkbook(workbook, partiallyInWorkbook), "Sheet(s) [Oak] do not exist in the current workbook; they could not be deleted.");

        //Verify that the sheet names that did exist have been removed
        //And that correct lists generate an empty string
        verify(workbook, times(1)).removeSheet("foo");
        assertEquals(service.notInWorkbook(workbook, singletonInWorbook), "");
        verify(workbook, times(2)).removeSheet("foo");
        assertEquals(service.notInWorkbook(workbook, exactlyInWorkbook), "");
        verify(workbook, times(3)).removeSheet("foo");
        verify(workbook, times(1)).removeSheet("bar");
        verify(workbook, times(4)).removeSheet(anyString());
    }

    /**
     * Tests if an exception is thrown when a read-only sheet is tried to be deleted
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".* is read-only")
    public void removeSheetsReadonly() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(true);
        ExcelServiceImpl service = new ExcelServiceImpl(null);
        service.removeSheets(workbook, null);
    }

    /**
     * Tests if an Illegal Argument Exception is thrown when sheets are tried to be removed while
     * they do not exist.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            "Sheet\\(s\\) \\[Apple\\] do not exist in the current workbook\\; they could not be deleted\\.")
    public void removeSheetsInvalidNames() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(false);
        when(workbook.fileExists()).thenReturn(true);
        when(workbook.getSheetNames()).thenReturn(Arrays.asList("foo", "Pear"));
        ExcelServiceImpl service = new ExcelServiceImpl(null);
        service.removeSheets(workbook, Arrays.asList("foo", "Apple"));
    }

    /**
     * Test that no exception is thrown when a list of existing sheet names should be removed from a workbook
     *
     * @throws Exception
     */
    @Test
    public void removeSheets() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(false);
        when(workbook.fileExists()).thenReturn(true);
        when(workbook.getSheetNames()).thenReturn(Arrays.asList("Pear", "Banana"));
        ExcelServiceImpl service = new ExcelServiceImpl(null);
        service.removeSheets(workbook, Arrays.asList("Pear", "Banana"));
    }

    /**
     * Test that an IllegalArgumentException is thrown when it is tried to save a read-only file.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Cannot write to this file: read-only")
    public void saveReadOnly() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        ExcelService service = new ExcelServiceImpl(null);
        File file = mock(File.class);

        XillWorkbook clonedBook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(true);
        when(workbook.createCopy(any(File.class))).thenReturn(clonedBook);
        service.save(workbook);
    }

    /**
     * Test that the correct method is called to continue the save operation and that the correct
     * result is returned.
     *
     * @throws Exception
     */
    @Test
    public void save() throws Exception {
        // Mock
        ExcelService service = new ExcelServiceImpl(null);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(false);
        XillWorkbook clonedBook = mock(XillWorkbook.class);
        when(workbook.createCopy(any(File.class))).thenReturn(clonedBook);
        File file = mock(File.class);
        when(file.getCanonicalPath()).thenReturn("thispath");

        // Run
        XillWorkbook newBook = service.save(workbook, file);

        // Verify
        assertEquals(newBook, clonedBook);
        verify(workbook, times(1)).createCopy(file);
    }

    /**
     * Test that an IllegalArgumentException is thrown when the save function (with a path to save it to) is called on
     * a read-only file
     *
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Cannot write to this file: read-only")
    public void saveOverrideReadOnly() throws Exception {
        ExcelService service = new ExcelServiceImpl(null);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(true);
        service.save(workbook);
    }

    /**
     * Test that the correct result is returned, the correct method is called and no exception is thrown when
     * the save operation is given a correct path
     *
     * @throws Exception
     */
    @Test
    public void saveOverride() throws Exception {
        ExcelService service = new ExcelServiceImpl(null);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(false);
        when(workbook.getLocation()).thenReturn("location");
        assertEquals(service.save(workbook), "Saved [location]");
        verify(workbook, times(1)).save();
    }

    /**
     * Checks if an exception is thrown when trying to remove a sheet form a readonly workbook
     */
    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "workbook is read-only")
    public void removeReadOnlySheet() {
        ExcelServiceImpl service = new ExcelServiceImpl(null);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(workbook.isReadOnly()).thenReturn(true);
        when(workbook.getFileString()).thenReturn("workbook");
        service.removeSheet(workbook, "");
    }

}
