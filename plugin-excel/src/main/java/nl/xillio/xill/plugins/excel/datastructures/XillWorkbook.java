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

import com.google.common.io.Files;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.plugins.excel.NoSuchSheetException;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of an Excel workbook.
 * Wrapper for the Apache POI {@link XSSFWorkbook}, {@link HSSFWorkbook} and {@link Workbook} classes.
 *
 * @author Daan Knoope
 */
public class XillWorkbook implements MetadataExpression {
    private Workbook workbook;
    private File file;
    private boolean isReadOnly = false;
    private String location;
    private CellStyle dateCellStyle;
    private CellStyle dateTimeCellStyle;

    /**
     * Constructor for the XillWorkbook class.
     *
     * @param workbook a workbook
     * @param file     a file where the workbook is (or will be) stored
     * @throws IOException if operations on the file did not succeed
     */
    public XillWorkbook(Workbook workbook, File file) throws IOException {
        this.workbook = workbook;
        this.file = file;
        location = file.getCanonicalPath();
        isReadOnly = file.exists() && !file.canWrite();
    }

    /**
     * Get this workbook's cell style for date formatting (no time).
     *
     * @return a style for date formatting
     */
    public CellStyle getDateCellStyle() {
        if (dateCellStyle == null) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));
        }
        return dateCellStyle;
    }

    /**
     * Get the workbook's cell style for date-time formatting.
     *
     * @return a style for date-time formatting
     */
    public CellStyle getDateTimeCellStyle() {
        if (dateTimeCellStyle == null) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            dateTimeCellStyle = workbook.createCellStyle();
            dateTimeCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy hh:mm"));
        }
        return dateTimeCellStyle;
    }


    /**
     * Returns a string with information for the debugging window.
     *
     * @return the string for the debugging window
     */
    public String getFileString() {
        return "Excel Workbook [" + location + "]";
    }

    /**
     * Gets the sheet with the name provided.
     *
     * @param sheetName the name of the sheet which should be retrieved
     * @return the sheet which was retrieved from this workbook
     * @throws NoSuchSheetException if the name of the sheet cannot be found in this workbook
     */
    public XillSheet getSheet(String sheetName) throws NoSuchSheetException {
        if (workbook.getSheetIndex(sheetName) == -1)
            throw new NoSuchSheetException("Sheet [" + sheetName + "] cannot be found in the supplied workbook");
        return new XillSheet(workbook.getSheet(sheetName), isReadOnly, this);
    }

    /**
     * Gets the sheet at the given index.
     *
     * @param index the index of the sheet which should be retrieved
     * @return the sheet which was retrieved from this workbook
     * @throws NoSuchSheetException if the index is out of bounds
     */
    public XillSheet getSheetAt(int index) throws NoSuchSheetException {
        if (index < 0 || index >= workbook.getNumberOfSheets()) {
            throw new NoSuchSheetException("Sheet index must be greater than 0 and smaller than the amount of sheets.");
        }
        return new XillSheet(workbook.getSheetAt(index), isReadOnly, this);
    }

    /**
     * Creates a new sheet in this workbook.
     *
     * @param sheetName the name of the new sheet
     * @return the sheet which has been created
     */
    public XillSheet makeSheet(String sheetName) {
        return new XillSheet(workbook.createSheet(sheetName), isReadOnly, this);
    }

    /**
     * Gets a list of the name of each of the sheets in this workbook.
     *
     * @return a list of names
     */
    public List<String> getSheetNames() {
        List<String> sheetnames = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++)
            sheetnames.add(workbook.getSheetAt(i).getSheetName());
        return sheetnames;
    }

    /**
     * Returns whether this workbook is readonly.
     *
     * @return whether this workbook is read-only
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Checks whether the file where this workbook is stored still exists on the storage device.
     *
     * @return whether the file still exists
     */
    public boolean fileExists() {
        return file.exists();
    }

    /**
     * Gets the location where this workbook is stored.
     *
     * @return a path as string
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Removes a sheet by its name from this workbook.
     *
     * @param sheetName the name of the sheet which should be deleted
     * @throws IllegalArgumentException if the sheet's name does not exist (anymore) in this workbook
     */
    public void removeSheet(String sheetName) {
        int sheetIndex = workbook.getSheetIndex(sheetName);
        if (sheetIndex == -1) {
            throw new IllegalArgumentException("Sheet " + sheetName + " does not exist in this workbook");
        }
        workbook.removeSheetAt(workbook.getSheetIndex(sheetName));
    }

    public FileOutputStream getOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    /**
     * Saves the workbook by overwriting the existing file.
     *
     * @throws IOException when the write operation could not succeed
     */
    public void save() throws IOException {
        save(file);
    }

    /**
     * Saves the workbook to the new location specified in the file. All sheets are recalculated
     * before the workbook is saved.
     *
     * @param file the new location of the Excel workbook
     * @throws IOException when the write operation could not succeed
     */
    public void save(File file) throws IOException {
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        file.getParentFile().mkdirs();
        try (OutputStream outputStream = getOutputStream(file)) {
            workbook.write(outputStream);
        }
    }

    /**
     * Creates a copy of the current workbook at a new location on the file system.
     *
     * @param file the target file where the copy will be written
     * @return a clone of this workbook, but written to the new location
     * @throws IllegalArgumentException if there is a mismatch between the new extension
     *                                  and the current extension of the workbook
     * @throws IOException              if a file is being written to itself
     * @throws IOException              if the IO operation could not succeed
     */
    public XillWorkbook createCopy(File file) throws IOException {
        String extension = FilenameUtils.getExtension(file.getName());
        String currentExtension = workbook instanceof HSSFWorkbook ? "xls" : "xlsx";
        if (!(currentExtension.equals(extension))) {
            throw new IllegalArgumentException("New file should have the same extension as original (" + currentExtension + ", not " + extension + ")");
        }

        this.save(file);

        file.setWritable(true);
        XillWorkbookFactory factory = getFactory();
        return factory.loadWorkbook(file);
    }

    //Wrappers for unit testing
    //Classes underneath are only called to make the code testable

    void copy(File origin, File destination) throws IOException {
        Files.copy(origin, destination);
    }

    XillWorkbookFactory getFactory() {
        return new XillWorkbookFactory();
    }

    /**
     * @return current workbook
     *
     * The method scope is intentionally set for package-private
     */
    Workbook getWorkbook() {
        return workbook;
    }
}

