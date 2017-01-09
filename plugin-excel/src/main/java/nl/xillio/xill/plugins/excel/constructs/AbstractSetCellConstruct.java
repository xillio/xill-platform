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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillCellRef;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;

import java.util.regex.Pattern;

/**
 * Base class for constructs setting an Excel cell.
 *
 * @author Daan Knoope
 * @author Geert Konijnendijk
 */
public abstract class AbstractSetCellConstruct extends AbstractExcelConstruct {

    /**
     * Changes the value of a given cell.
     *
     * @param sheet   the sheet which' cell needs to be changed
     * @param column  the column of the cell which should be changed
     * @param row     the row of the cell which should be changed
     * @param value   the value to which the cell should be set
     * @param formula Whether the value represents a formula
     * @return {@code true} when the operation has succeeded, else an exception is thrown
     * @throws RobotRuntimeException when the sheet is read-only
     * @throws RobotRuntimeException when a wrong notation for the row has been used (should be numeric)
     * @throws RobotRuntimeException when a wrong notation for the column has been used (should be numeric or alphabetic)
     */
    static MetaExpression setCell(MetaExpression sheet, MetaExpression column, MetaExpression row, MetaExpression value, MetaExpression formula) {
        XillSheet xillSheet = assertMeta(sheet, "parameter 'sheet'", XillSheet.class, "result of loadSheet or createSheet");
        if (xillSheet.isReadonly()) {
            throw new RobotRuntimeException("Cannot write on sheet: sheet is read-only. First save as new file.");
        }
        if (!isNumeric(row)) {
            throw new RobotRuntimeException("Wrong notation for row \"" + row.getStringValue() + "\", should be numeric (e.g. 12)");
        }
        if (!isNumericXORAlphabetic(column)) {
            throw new RobotRuntimeException("Wrong notation for column \"" + column.getStringValue() + "\", should be numerical or alphabetical notation (e.g. AB or 12)");
        }
        trySetCell(column, row, value, formula, xillSheet);
        return TRUE;
    }

    /**
     * Tries to set the value of a cell.
     *
     * @param column    the column of the cell to set
     * @param row       the row of the cell to set
     * @param value     the value to set the cell to
     * @param formula   whether the value represents a formula
     * @param xillSheet the sheet to set the value in
     * @throws RobotRuntimeException When setting the cell fails
     */
    private static void trySetCell(MetaExpression column, MetaExpression row, MetaExpression value, MetaExpression formula, XillSheet xillSheet) {
        try {
            XillCellRef cellRef = getCellRef(column, row);
            setValue(xillSheet, cellRef, value, formula);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Sets the value of a cell on a XillSheet.
     *
     * @param sheet   the XillSheet which contains the cell which should be changed
     * @param cellRef a XillCellRef pointing to the cell which should be changed
     * @param value   a MetaExpression containing the value which the cell should contain
     * @param formula a MetaExpression evaluating to True when value represents a formula and false otherwise
     */
    protected static void setValue(XillSheet sheet, XillCellRef cellRef, MetaExpression value, MetaExpression formula) {
        if (value.isNull()) {
            sheet.emptyCellValue(cellRef);
        } else if (formula.getBooleanValue()) {
            sheet.setCellValue(cellRef, value.getStringValue(), true);
        } else {
            nl.xillio.xill.api.data.Date date = value.getMeta(nl.xillio.xill.api.data.Date.class);
            Object objectValue = MetaExpression.extractValue(value);

            if (date != null) {
                sheet.setCellValue(cellRef, date.getZoned());
            } else if (objectValue instanceof Boolean) {
                sheet.setCellValue(cellRef, value.getBooleanValue());
            } else if (objectValue instanceof Number) {
                sheet.setCellValue(cellRef, value.getNumberValue().doubleValue());
            } else {
                sheet.setCellValue(cellRef, value.getStringValue(), false);
            }
        }
    }

    /**
     * Creates a XillCellRef given the raw column and row notation.
     *
     * @param column points to the column of the cell which should be changed, either in alphabetic or numeric notation (e.g. "AB" or 28)
     * @param row    points to the row of the cell which should be changed, in numeric notation (e.g. 28)
     * @return a XillCellRef pointing to the row and column of the cell
     */
    protected static XillCellRef getCellRef(MetaExpression column, MetaExpression row) {
        if (isNumeric(column)) {
            return new XillCellRef(column.getNumberValue().intValue(), row.getNumberValue().intValue());
        }
        return new XillCellRef(column.getStringValue(), row.getNumberValue().intValue());
    }

    /**
     * Checks if a MetaExpression is either numeric or alphabetic (exclusive or)
     *
     * @param expression the MetaExpression to check
     * @return whether the input is numeric
     */
    protected static boolean isNumericXORAlphabetic(MetaExpression expression) {
        return Pattern.matches("[a-zA-Z]*|[0-9]*", expression.getStringValue());
    }

}
