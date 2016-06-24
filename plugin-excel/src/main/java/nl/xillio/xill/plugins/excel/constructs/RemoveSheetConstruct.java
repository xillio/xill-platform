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

import com.google.inject.Inject;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import nl.xillio.xill.plugins.excel.services.ExcelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Construct to remove one or more sheets from the provided workbook.
 *
 * @author Daan Knoope
 */
public class RemoveSheetConstruct extends Construct {

    @Inject
    private ExcelService excelService;

    /**
     * Processes the xill code to remove one sheet or a list of sheets from the provided workbook.
     *
     * @param service       The {@link ExcelService} provided by the construct.
     * @param workbookInput The workbook object created by
     *                      {@link CreateWorkbookConstruct} or {@link LoadWorkbookConstruct} from
     *                      which the sheets should be deleted
     * @param sheetName     the name of the sheet that should be deleted, or a list thereof
     * @return {@code true} when it succeeds, else an exception
     * @throws RobotRuntimeException when no valid (list of) sheet names has been provided
     * @throws RobotRuntimeException when the workbook is read-only
     * @throws RobotRuntimeException when some or all of the sheet names do not exist in the
     *                               workbook (all the sheets that did exist have been deleted when the exception is thrown)
     */
    static MetaExpression process(ExcelService service, MetaExpression workbookInput, MetaExpression sheetName) {
        XillWorkbook workbook = assertMeta(workbookInput, "parameter 'workbook'", XillWorkbook.class, "result of loadWorkbook or createWorkbook");

        if (sheetName.getType() == ExpressionDataType.ATOMIC) {
            return processSingle(service, workbook, sheetName);
        } else if (sheetName.getType() == ExpressionDataType.LIST) {
            return processMultiple(service, workbook, sheetName);
        } else {
            throw new RobotRuntimeException("No valid (list of) sheetnames provided");
        }

    }

    /**
     * Deletes a single sheet from a XillWorkbook.
     *
     * @param service   the {@link ExcelService} provided by the construct
     * @param workbook  the {@link XillWorkbook} from which the sheet should be deleted
     * @param sheetName the name of the {@link XillSheet} which should be removed
     * @return {@code true} when it has succeeded, else an exception is thrown
     * @throws RobotRuntimeException when the sheet is not in the provided workbook
     * @throws RobotRuntimeException when the workbook is read-only
     */
    static MetaExpression processSingle(ExcelService service, XillWorkbook workbook, MetaExpression sheetName) {
        try {
            service.removeSheet(workbook, sheetName.getStringValue());
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
        return fromValue(true);
    }

    /**
     * Deletes a list of sheets from a XillWorkbook.
     *
     * @param service         the {@link ExcelService} provided by the construct
     * @param workbook        the (@link XillWorkbook) from which the sheet(s) should be deleted
     * @param sheetNamesInput the list of names of the {@link XillSheet}s that should be removed
     * @return {@code true} when it has succeeded, else an exception is thrown
     * @throws RobotRuntimeException when one or more of the sheets is not in the provided workbook
     * @throws RobotRuntimeException when the workbook is read-only
     */
    @SuppressWarnings("unchecked")
    static MetaExpression processMultiple(ExcelService service, XillWorkbook workbook, MetaExpression sheetNamesInput) {
        //Unchecked cast warning is here suppressed because cast is actually checked in the constructprocessor
        //And process method, but compiler does not see this.
        List<String> sheetNames = ((List<MetaExpression>) (sheetNamesInput.getValue())).stream().
                map(MetaExpression::getStringValue).collect(Collectors.toList());
        try {
            service.removeSheets(workbook, sheetNames);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
        return fromValue(true);
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (workbook, sheet) -> process(excelService, workbook, sheet),
                new Argument("workbook", ATOMIC),
                new Argument("sheetName", ATOMIC, LIST));
    }

}
