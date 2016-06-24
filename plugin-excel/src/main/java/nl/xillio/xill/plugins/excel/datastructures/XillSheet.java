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

import nl.xillio.xill.api.data.MetadataExpression;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Representation of an Excel sheet.
 * Wrapper for the Apache POI {@link Sheet} class.
 *
 * @author Daan Knoope
 */
public class XillSheet implements MetadataExpression {
    private final XillWorkbook workbook;
    private Sheet sheet;
    private String name;
    private int columnLength;
    private int rowLength;
    private boolean readonly;

    /**
     * Constructor for the XillSheet class.
     *
     * @param sheet    an Apache POI {@link Sheet}
     * @param readonly whether the sheet's workbook is read-only
     * @param workbook the parent workbook of this sheet
     */
    public XillSheet(Sheet sheet, boolean readonly, XillWorkbook workbook) {
        this.readonly = readonly;
        this.sheet = sheet;

        calculateRowLength();
        columnLength = calculateColumnLength();
        name = sheet.getSheetName();

        this.workbook = workbook;
    }

    XillWorkbook getParentWorkbook() {
        return this.workbook;
    }

    int calculateRowLength() {
        if (sheet.getLastRowNum() == 0 && sheet.getPhysicalNumberOfRows() == 0) {
            rowLength = 0;
        } else {
            rowLength = sheet.getLastRowNum() + 1;
        }
        return rowLength;
    }

    /**
     * Calculates the width of the spreadsheet.
     * Use {@link #getColumnLength()} to get the width of the spreadsheet: this method should
     * only be called by the constructor since it is very CPU intensive.
     *
     * @return an integer representing the width of the spreadsheet.
     */
    int calculateColumnLength() {
        //CPU intensive, use only once, then use the columnLength property
        int maxColumnSize = 0;
        for (int i = 0; i < rowLength; i++) {
            if (sheet.getRow(i) != null && maxColumnSize < getRow(i).getLastCellNum()) {
                maxColumnSize = getRow(i).getLastCellNum();
            }
        }
        return maxColumnSize;
    }

    /**
     * Gets the width of the spreadsheet.
     *
     * @return the highest column size as integer
     */
    public int getColumnLength() {
        return columnLength;
    }

    /**
     * Gets the height of the spreadsheet.
     *
     * @return the amount of rows of the spreadsheet
     */
    public int getRowLength() {
        return rowLength;
    }

    /**
     * Gets the name of the current sheet.
     *
     * @return name as string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of a cell, of type {@link String}, {@link Boolean}, {@link Double} or {@link Date}.
     *
     * @param cellRef a {@link XillCellRef} pointing to the required cell
     * @return the value of the cell referred to by the {@link XillCellRef}.
     */
    public Object getCellValue(XillCellRef cellRef) {
        XillRow row = getRow(cellRef.getRow());
        return row.isNull() ? new XillCell(null, this).getValue() : row.getCell(cellRef.getColumn(), this).getValue();
    }

    /**
     * Gets a row.
     *
     * @param rowNr the number of the row which should be retrieved
     * @return the row which was requested
     */
    public XillRow getRow(int rowNr) {
        return new XillRow(sheet.getRow(rowNr));
    }

    /**
     * Creates a new row or overwrites it when one already exists.
     *
     * @param rowNr the y-coordinate where the new row should be created
     * @return the new row
     */
    XillRow createRow(int rowNr) {
        return new XillRow(sheet.createRow(rowNr));
    }

    /**
     * Gets the cell when the cell already exists
     * and otherwise creates a new one at the given location.
     *
     * @param cellRef a reference to the cell which should be returned
     * @return the cell which should be returned
     */
    XillCell getCell(XillCellRef cellRef) {
        int columnNr = cellRef.getColumn();
        int rowNr = cellRef.getRow();

        XillRow row = getRow(rowNr);
        if (row.isNull()) { //create new row if non-existent
            row = createRow(rowNr);
        }
        XillCell cell = row.getCell(columnNr, this);
        if (cell.isNull()) { //create new cell if non-existent
            cell = row.createCell(columnNr, this);
        }

        return cell;
    }

    /**
     * Sets the value of the cell.
     *
     * @param cellRef   reference to the cell which should be changed
     * @param value     string value (could be formula) which should be put in the cell
     * @param isFormula true if the value is treated as formula, otherwise the value is stored as string
     */
    public void setCellValue(XillCellRef cellRef, String value, boolean isFormula) {
        getCell(cellRef).setCellValue(value, isFormula);
        calculateRowAndColumnLength(cellRef);
    }

    /**
     * Sets the value of the cell.
     *
     * @param cellRef reference to the cell which should be changed
     * @param value   double value which should be put in the cell
     */
    public void setCellValue(XillCellRef cellRef, Double value) {
        getCell(cellRef).setCellValue(value);
        calculateRowAndColumnLength(cellRef);
    }

    /**
     * Sets the value of the cell.
     *
     * @param cellRef reference to the cell which should be changed
     * @param value   boolean value which should be put in the cell
     */
    public void setCellValue(XillCellRef cellRef, boolean value) {
        getCell(cellRef).setCellValue(value);
        calculateRowAndColumnLength(cellRef);
    }

    /**
     * Sets the value of the cell.
     *
     * @param cellRef reference to the cell which should be changed
     * @param value   dateTime value which should be put in the cell
     */
    public void setCellValue(XillCellRef cellRef, ZonedDateTime value) {
        getCell(cellRef).setCellValue(value);
        calculateRowAndColumnLength(cellRef);
    }

    /**
     * Sets the value of the cell to empty.
     *
     * @param cellRef reference to the cell which should be changed
     */
    public void emptyCellValue(XillCellRef cellRef) {
        getCell(cellRef).setNull();
        calculateRowAndColumnLength(cellRef);
    }

    private void calculateRowAndColumnLength(final XillCellRef cellRef) {
        calculateRowLength();
        if (cellRef.getColumn() + 1 > columnLength) {
            columnLength = cellRef.getColumn() + 1;
        }
    }

    /**
     * Returns if sheet is in a read-only workbook.
     *
     * @return whether the sheet is in a read-only workbook
     */
    public boolean isReadonly() {
        return readonly;
    }

}
