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
import nl.xillio.xill.plugins.excel.datastructures.XillRow;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Iterator;

/**
 * An iterator over a row.
 *
 * @author Xavier Pardonnet
 */
public class GetRowConstruct extends AbstractExcelConstruct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {

        return new ConstructProcessor(
                this::process,
                new Argument(PARAMETER_NAME_SHEET, OBJECT),
                new Argument(PARAMETER_NAME_ROW, ATOMIC));
    }

    protected MetaExpression process(MetaExpression sheetExpression, MetaExpression rowExpression) {
        XillSheet sheet = assertMeta(sheetExpression, PARAMETER_NAME_SHEET, XillSheet.class, "Excel sheet");
        int rowNum = assertNumeric(rowExpression, "row");
        if (rowNum < 1) {
            throw new RobotRuntimeException("The row number must be greater than one (" + rowNum + " was used)");
        }

        MetaExpression result = fromValue("Cells in row " + rowNum);

        XillRow row = sheet.getRow(rowNum - 1);
        Iterator<Cell> iterator = row.iterator();

        result.storeMeta(new MetaExpressionIterator<>(iterator, cell -> fromCellValue(sheet.getCellValue(new XillCellRef(cell)))));
        return result;
    }

}
