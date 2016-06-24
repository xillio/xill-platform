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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Iterator;

/**
 * Representation for an Excel Row.
 * Wrapper for the Apache POI {@link Row} class.
 *
 * @author Daan Knoope
 */
public class XillRow {
    private Row row;

    /**
     * Default constructor.
     *
     * @param row the Apache POI Row to wrap
     */
    public XillRow(Row row) {
        this.row = row;
    }

    /**
     * Gets a XillCell from this row.
     *
     * @param columnNr the number of the column where the cell is located in this row
     * @param sheet    the XillSheet to which this row belongs
     * @return the XillCell on the provided column
     */
    public XillCell getCell(int columnNr, XillSheet sheet) {
        return new XillCell(row.getCell(columnNr), sheet);
    }

    public boolean isNull() {
        return row == null;
    }

    /**
     * Creates a cell given the column number where it should be.
     * Creates a new cell at the same location (overwriting the old cell) if one already exists.
     *
     * @param columnNr the number of the column where the new cell should be located
     * @param sheet    the XillSheet where the new cell should be located
     * @return the newly created XillCell
     * @throws IllegalArgumentException if columnIndex &lt; 0 or greater
     *                                  than the maximum number of supported columns (255 for *.xls, 1048576
     *                                  for *.xlsx)
     */
    public XillCell createCell(int columnNr, XillSheet sheet) {
        return new XillCell(row.createCell(columnNr), sheet);
    }

    /**
     * Gets the column number of the last cell in this row.
     *
     * @return the column number of the last cell in this row
     */
    public int getLastCellNum() {
        return row.getLastCellNum();
    }

    /**
     * Returns the row's iterator.
     *
     * @return the row's iterator
     */
    public Iterator<Cell> iterator() {
        return row.iterator();
    }
}
