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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

/**
 * Representation of a reference to an Excel cell.
 * Wrapper for the Apache POI {@link CellReference} class.
 *
 * @author Daan Knoope
 */
public class XillCellRef implements MetadataExpression {

    private CellReference cellReference;

    public XillCellRef(Cell cell) {
        cellReference = new CellReference(cell);
    }

    /**
     * Constructs a XillCellRef based on 1-indexed references
     *
     * @param column a string pointing to the column of the cell
     *               in alphabetic notation (e.g. "AB"),
     *               1-indexed (Excel notation)
     * @param row    an integer pointing to the row of the cell in
     *               numeric notation (e.g. 28), 1-indexed
     *               (Excel notation)
     */
    public XillCellRef(String column, int row) {
        if (row < 1) {
            throw new IllegalArgumentException("The row number must be greater than one (" + row + " was used)");
        }
        cellReference = new CellReference(column + row); //A, 12 => A12
    }

    /**
     * Constructor for the XillCellRef class
     *
     * @param column an integer pointing to the column of the cell
     *               in numeric notation (e.g. 28), 1-indexed
     *               (Excel notation)
     * @param row    an integer pointing to the row of the cell
     *               in numeric notation (e.g. 28), 1-indexed (Excel notation)
     */
    public XillCellRef(int column, int row) {
        if (row < 1) {
            throw new IllegalArgumentException("The row number must be greater than one (" + row + " was used)");
        }
        if (column < 1) {
            throw new IllegalArgumentException("The column number must be greater than one (" + column + " was used)");
        }
        cellReference = new CellReference(row - 1, column - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XillCellRef cellRef = (XillCellRef) o;

        return !(cellReference != null ? !cellReference.equals(cellRef.cellReference) : cellRef.cellReference != null);

    }

    @Override
    public int hashCode() {
        return cellReference != null ? cellReference.hashCode() : 0;
    }

    /**
     * Gets the number of the Excel column, 0-indexed
     *
     * @return the column of the reference as integer
     */
    public int getColumn() {
        return cellReference.getCol();
    }

    /**
     * Gets the number of the Exel row, 0-indexed
     *
     * @return the row of the reference as integer, 0-indexed
     */
    public int getRow() {
        return cellReference.getRow();
    }

    @Override
    public String toString() {
        return cellReference.toString();
    }
}
