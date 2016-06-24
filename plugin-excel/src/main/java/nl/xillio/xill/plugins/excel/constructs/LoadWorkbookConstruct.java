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
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import nl.xillio.xill.plugins.excel.services.ExcelService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;

/**
 * Construct to load a XillWorkbook from an existing file.
 *
 * @author Daan Knoope
 */
public class LoadWorkbookConstruct extends Construct {

    @Inject
    private ExcelService excelService;

    /**
     * Processes the xill code to load a XillWorkook from the given filepath.
     *
     * @param excelService the {@link ExcelService} provided by the construct
     * @param context      the {@link ConstructContext} provided by the construct
     * @param filePath     a (relative or absolute path) to where the Excel file is located
     * @return a {@link XillWorkbook} stored in a string pointing to the absolute path
     * where it has been loaded from
     * @throws RobotRuntimeException the path does not lead to an xls or xlsx file
     * @throws RobotRuntimeException there is no file at the given path
     * @throws RobotRuntimeException the file could not be opened
     */
    static MetaExpression process(ExcelService excelService, ConstructContext context, MetaExpression filePath) {

        String path = filePath.getStringValue();
        File file = getFile(context, path);
        String workbookText;
        XillWorkbook workbook;
        try {
            workbook = excelService.loadWorkbook(file);
            workbookText = workbook.getFileString();
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException("Path does not lead to an xls or xlsx Microsoft Excel file", e);
        } catch (FileNotFoundException e) {
            throw new RobotRuntimeException("There is no file at the given path", e);
        } catch (InvalidObjectException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RobotRuntimeException("File could not be opened", e);
        }

        if (workbook.isReadOnly())
            context.getRootLogger().warn("Opened in read-only mode.");

        MetaExpression returnValue = fromValue(workbookText);
        returnValue.storeMeta(workbook);
        return returnValue;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                a -> process(excelService, context, a),
                new Argument("filePath", ATOMIC));
    }
}
