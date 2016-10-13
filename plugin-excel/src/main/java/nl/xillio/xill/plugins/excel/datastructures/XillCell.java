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
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.ZonedDateTime;
import java.util.Date;


/**
 * Representation of an Excel cell.
 * Wrapper for the Apache POI {@link Cell} class.
 *
 * @author Daan Knoope
 */
public class XillCell {

    private Cell cell;
    private final XillSheet sheet;

    /**
     * Constructor for the XillCell class.
     *
     * @param cell  an Apache POI {@link Cell} object
     * @param sheet the parent {@link XillSheet} of this cell
     */
    public XillCell(Cell cell, XillSheet sheet) {

        this.cell = cell;
        this.sheet = sheet;
    }

    boolean isDateFormatted() {
        return DateUtil.isCellDateFormatted(cell);
    }

    XillSheet getParentSheet() {
        return this.sheet;
    }

    /**
     * Gets the value of this cell.
     *
     * @return the value of this cell as an object. Can be: {@link String}, {@link Boolean}, {@link Double} or {@link Date}.
     * @throws NotImplementedException when this cell has an unsupported formatting
     */
    public Object getValue() {
        Object toReturn;
        if (!isNull()) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    toReturn = cell.getRichStringCellValue().getString();
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    toReturn = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    toReturn = cell.getCellFormula();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    toReturn = parseNumericValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    toReturn = null;
                    break;
                default:
                    throw new NotImplementedException("A cell format that has been used in the Excel file is currently unsupported.");
            }
        } else {
            toReturn = null;
        }
        return toReturn;
    }

    /**
     * Parse a numeric value from this cell, the cell type should be {@link Cell#CELL_TYPE_NUMERIC}.
     *
     * @return A {@link DateImpl} if the cell is formatted as a date, an Integer if the number has no decimal places or a Double
     */
    private Object parseNumericValue() {
        Object toReturn;
        if (isDateFormatted()) {
            toReturn = new DateImpl(cell.getDateCellValue());
        } else {
            Double temp = cell.getNumericCellValue();
            // Check if temp is an integer or double.
            if (Double.doubleToRawLongBits(Math.floor(temp) - temp) == 0 && Math.abs(temp) < Long.MAX_VALUE) {
                toReturn = temp.longValue();
            } else {
                toReturn = temp;
            }
        }
        return toReturn;
    }

    public boolean isNull() {
        return cell == null;
    }

    /**
     * Set this cell to null value
     */
    public void setNull() {
        if (!isNull()) {
            cell.setCellType(Cell.CELL_TYPE_BLANK);
        }
    }

    /**
     * Sets this cell's value to the provided String
     *
     * @param value     the string value which should be stored in this cell. Can be made a formula by
     *                  staring the string off with an equals sign (=).
     * @param isFormula true if the value is treated as formula, otherwise the value is stored as string
     */
    public void setCellValue(String value, boolean isFormula) {
        if (isFormula) {
            try {
                cell.setCellType(Cell.CELL_TYPE_FORMULA);
                cell.setCellFormula(value);
            } catch (FormulaParseException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        } else {
            // Set the cell to text explicitly to prevent the value being parsed as a formula
            if (value.startsWith("=")) {
                Workbook workbook = sheet.getParentWorkbook().getWorkbook();
                if (workbook instanceof XSSFWorkbook) {
                    XSSFWorkbook book = (XSSFWorkbook) workbook;
                    XSSFCellStyle style = book.createCellStyle();
                    style.setDataFormat((short) BuiltinFormats.getBuiltinFormat("text"));
                    cell.setCellStyle(style);
                } else if (workbook instanceof HSSFWorkbook) {
                    HSSFWorkbook book = (HSSFWorkbook) workbook;
                    HSSFCellStyle style = book.createCellStyle();
                    style.setDataFormat((short) BuiltinFormats.getBuiltinFormat("text"));
                    cell.setCellStyle(style);
                } else {
                    throw new RobotRuntimeException("Cannot set cell " + cell.getColumnIndex() + ":" + cell.getRowIndex() + " to a value starting with \"=\" in this workbook type");
                }
            }
            cell.setCellValue(value);
        }
    }

    /**
     * Sets the cell's value to the provided Double
     *
     * @param value the double which should be stored in this cell
     */
    public void setCellValue(Double value) {
        cell.setCellValue(value);
    }

    /**
     * Sets the cell's value to the provided boolean
     *
     * @param value the boolean which should be stored in this cell
     */
    public void setCellValue(boolean value) {
        cell.setCellValue(value);
    }

    /**
     * Sets the cell's value to the provided date time
     *
     * @param dateTime the datevalue that should be stored in the cell
     */
    public void setCellValue(ZonedDateTime dateTime) {
        //Assumption: 0:00 means no time
        boolean containsTime = !(dateTime.getHour() == 0 && dateTime.getMinute() == 0);
        if (containsTime) {
            cell.setCellValue(Date.from(dateTime.toInstant()));
            CellStyle dateTimeStyle = this.sheet.getParentWorkbook().getDateTimeCellStyle();
            this.cell.setCellStyle(dateTimeStyle);
        } else {
            cell.setCellValue(Date.from(dateTime.toInstant()));
            CellStyle dateStyle = this.sheet.getParentWorkbook().getDateCellStyle();
            this.cell.setCellStyle(dateStyle);
        }
    }

}
