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
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillCellRef;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over a column.
 *
 * @author Xavier Pardonnet
 */
public class GetColumnConstruct extends AbstractExcelConstruct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument(PARAMETER_NAME_SHEET, OBJECT),
                new Argument(PARAMETER_NAME_COLUMN, ATOMIC));
    }

    protected MetaExpression process(MetaExpression sheetExpression, MetaExpression columnExpression) {
        XillSheet sheet = assertMeta(sheetExpression, PARAMETER_NAME_SHEET, XillSheet.class, "Excel sheet");

        MetaExpression result = fromValue("Cells in column " + columnExpression.getStringValue());

        final int maxRow = sheet.getRowLength();

        Iterator<XillCellRef> iterator;
        if (isNumeric(columnExpression)) {
            final int colNum = assertNumeric(columnExpression, PARAMETER_NAME_COLUMN);
            if (colNum < 1) {
                throw new RobotRuntimeException("The column number must be superior to one (" + colNum + " was used)");
            }
            iterator = new ColumnIterator(maxRow, colNum);
        } else {
            final String colRef = assertAlpha(columnExpression, PARAMETER_NAME_COLUMN);
            iterator = new ColumnIterator(maxRow, colRef);
        }

        result.storeMeta(new MetaExpressionIterator<>(iterator, cell -> fromCellValue(sheet.getCellValue(cell))));
        return result;
    }

    /**
     * Iterator of cellRefs contained in a column. Had to be implemented here since the Apache POI implementation of a
     * column doesn't provide an iterator.
     */
    protected static class ColumnIterator implements Iterator<XillCellRef> {
        private final int maxRow;
        private Integer colNum = null;
        private String colRef = null;
        private int row = 1;

        /**
         * Constructor taking a numerical column notation.
         *
         * @param maxRow the maximum number of rows contained in this column
         * @param colNum the column number
         */
        public ColumnIterator(int maxRow, Integer colNum) {
            this.maxRow = maxRow;
            this.colNum = colNum;
        }

        /**
         * Constructor taking an alphabetic column notation.
         *
         * @param maxRow the maximum number of rows contained in this column
         * @param colRef the column reference
         */
        public ColumnIterator(int maxRow, String colRef) {
            this.maxRow = maxRow;
            this.colRef = colRef;
        }

        @Override
        public boolean hasNext() {
            return row <= maxRow;
        }

        @Override
        public XillCellRef next() {
            if (row > maxRow) {
                throw new NoSuchElementException("Iterator has no next element");
            }
            XillCellRef result = colRef == null ? new XillCellRef(colNum, row) : new XillCellRef(colRef, row);
            row++;
            return result;
        }
    }
}
