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

import com.google.inject.ImplementedBy;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Interface for the ExcelService which contains some of the functionality of the Excel plugin
 *
 * @author Daan Knoope
 */

@ImplementedBy(ExcelServiceImpl.class)
public interface ExcelService {
    /**
     * Loads a workbook from file.
     *
     * @param file a file that points to the location of the workbook which should be loaded
     * @return a new workbook containing the loaded Excel workbook
     * @throws FileNotFoundException    when the file could not be located
     * @throws IllegalArgumentException when the extension of the file is neither xls nor xlsx
     */
    XillWorkbook loadWorkbook(File file) throws IOException;

    /**
     * Creates a new workbook using the provided location.
     *
     * @param file a file pointing to the location where the new workbook should be stored
     * @return a new workbook
     * @throws IllegalArgumentException when the extension of the file is neither xls nor xlsx
     * @throws IOException              when writing to the location did not succeed
     */
    XillWorkbook createWorkbook(File file) throws IOException;

    /**
     * Creates a new sheet in the provided workbook.
     *
     * @param workbook  the workbook in which the new sheet should be stored
     * @param sheetName the name of the new sheet
     * @return the newly created sheet
     * @throws NullPointerException     when the provided workbook is incorrect
     * @throws IllegalArgumentException when the sheet name was invalid
     * @throws IllegalArgumentException when the workbook is read-only
     */
    XillSheet createSheet(XillWorkbook workbook, String sheetName);

    /**
     * Removes a sheet from the provided workbook.
     *
     * @param workbook  the workbook from which the sheet should be removed
     * @param sheetName the name of the sheet which should be removed
     * @throws IllegalArgumentException when the sheet's workbook is read-only
     * @throws IllegalArgumentException when the name of the sheet could not be found in the workbook
     */
    void removeSheet(XillWorkbook workbook, String sheetName);

    /**
     * Removes a list of sheets from the provided workbook.
     *
     * @param workbook   the workbook from which the sheet should be removed
     * @param sheetNames a list of names of the sheets which should be removed
     * @throws IllegalArgumentException when the workbook is read-only
     * @throws IllegalArgumentException when one or more sheets do not exist in this workbook
     */
    void removeSheets(XillWorkbook workbook, List<String> sheetNames);

    /**
     * Saves the provided workbook to the specified location.
     *
     * @param workbook the workbook which should be saved
     * @param file     a file to which the workbook can be written
     * @return the newly saved workbook
     * @throws IOException              when the write did not succeed
     * @throws IllegalArgumentException when the file is read-only
     */
    XillWorkbook save(XillWorkbook workbook, File file) throws IOException;

    /**
     * Saves the provided workbook by overwriting the old one.
     *
     * @param workbook the workbook which should be saved
     * @return a string containing the location of the workbook
     * @throws IOException              when the write did not succeed
     * @throws IllegalArgumentException when the file is read-only
     */
    String save(XillWorkbook workbook) throws IOException;
}
