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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbookFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for the ExcelService interface
 *
 * @author Daan Knoope
 */
@Singleton
public class ExcelServiceImpl implements ExcelService {

    private final XillWorkbookFactory factory;

    @Inject
    public ExcelServiceImpl(XillWorkbookFactory factory) {
        this.factory = factory;
    }

    @Override
    public XillWorkbook loadWorkbook(File file) throws IOException {
        String filePath = file.getCanonicalPath();
        if (!file.exists()) {
            throw new FileNotFoundException("There is no file at the given path");
        }
        if (!(filePath.endsWith(".xls") || filePath.endsWith(".xlsx"))) {
            throw new IllegalArgumentException("Path does not lead to an xls or xlsx Microsoft Excel file");
        }

        return factory.loadWorkbook(file);
    }

    @Override
    public XillWorkbook createWorkbook(File file) throws IOException {
        return factory.createWorkbook(file);
    }

    @Override
    public XillSheet createSheet(XillWorkbook workbook, String sheetName) {
        if (workbook == null) {
            throw new NullPointerException("The provided workbook is invalid");
        }
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException("No name was supplied: sheet names must be at least one character long");
        }
        if (sheetName.length() > 31) {
            throw new IllegalArgumentException("Sheet name is too long: must be less than 32 characters");
        }
        if (workbook.isReadOnly()) {
            throw new IllegalArgumentException("Workbook is read-only");
        }
        return workbook.makeSheet(sheetName);
    }

    @Override
    public void removeSheet(XillWorkbook workbook, String sheetName) {
        if (workbook.isReadOnly()) {
            throw new IllegalArgumentException(workbook.getFileString() + " is read-only");
        }
        workbook.removeSheet(sheetName);
    }

    /**
     * Returns an error message string containing the sheets that are not in the workbook (and thus cannot be deleted)
     *
     * @param workbook        the workbook from which the sheets should be deleted
     * @param inputSheetNames a list of sheet names which should be deleted
     * @return an error message containing the sheets that are not in the workbook, otherwise an empty string
     */
    String notInWorkbook(XillWorkbook workbook, List<String> inputSheetNames) {
        List<String> notInWorkbook = new ArrayList<>(inputSheetNames);
        notInWorkbook.removeAll(workbook.getSheetNames());
        List<String> inWorkbook = new ArrayList<>(inputSheetNames);
        inWorkbook.removeAll(notInWorkbook);
        inWorkbook.forEach(workbook::removeSheet);
        if (!notInWorkbook.isEmpty()) {
            String notRemoved = "Sheet(s) [";
            for (int i = 0; i < notInWorkbook.size() - 1; i++) {
                notRemoved += notInWorkbook.get(i) + ",";
            }
            notRemoved += notInWorkbook.get(notInWorkbook.size() - 1) + "] do not exist in the current workbook; they could not be deleted.";
            return notRemoved;
        } else {
            return "";
        }
    }

    @Override
    public void removeSheets(XillWorkbook workbook, List<String> inputSheetNames) {
        if (workbook.isReadOnly()) {
            throw new IllegalArgumentException(workbook.getFileString() + " is read-only");
        }
        String notInWorkbook = notInWorkbook(workbook, inputSheetNames);
        if (!notInWorkbook.isEmpty()) {
            throw new IllegalArgumentException(notInWorkbook);
        }
    }

    @Override
    public XillWorkbook save(XillWorkbook workbook, File file) throws IOException {
        return workbook.createCopy(file);
    }

    @Override
    public String save(XillWorkbook workbook) throws IOException {
        if (workbook.isReadOnly()) {
            throw new IllegalArgumentException("Cannot write to this file: read-only");
        }
        workbook.save();
        return "Saved [" + workbook.getLocation() + "]";
    }

}
