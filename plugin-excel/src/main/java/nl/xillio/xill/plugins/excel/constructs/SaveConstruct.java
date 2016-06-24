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
import java.io.IOException;

/**
 * Construct to save the current workbook (to another place).
 *
 * @author Daan Knoope
 */
public class SaveConstruct extends Construct {

    @Inject
    private ExcelService service;

    /**
     * Processes the xill code to save the provided workbook at the same or another path.
     *
     * @param service       the {@link ExcelService} provided by the construct
     * @param context       the {@link ConstructContext} provided by the construct
     * @param workbookInput a workbook object created by {@link CreateWorkbookConstruct} or
     *                      {@link LoadWorkbookConstruct} containing a {@link XillWorkbook}
     * @param path          optional: the path to where the workbook should be saved. If
     *                      no path is supplied, the file is saved to the same location
     * @return a string to the location where the file has been saved
     * @throws RobotRuntimeException when the file is read-only or cannot be written to
     */
    static MetaExpression process(ExcelService service, ConstructContext context, MetaExpression workbookInput, MetaExpression path) {
        XillWorkbook workbook = assertMeta(workbookInput, "parameter 'workbook'", XillWorkbook.class, "result of loadWorkbook or createWorkbook");
        if (path.isNull()) //default
            return processOverwrite(service, workbook);
        else {
            File file = getFile(context, path.getStringValue());
            return processToFolder(service, workbook, file);
        }
    }

    /**
     * Saves the workbook by overwriting the current file.
     *
     * @param service  the {@link ExcelService} provided by the construct
     * @param workbook the {@link XillWorkbook} which will be saved
     * @return a string pointing to the location where the file has been saved
     * @throws RobotRuntimeException when the file is read-only or cannot be written to
     */
    static MetaExpression processOverwrite(ExcelService service, XillWorkbook workbook) {
        String returnValue = "";
        try {
            returnValue = service.save(workbook);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
        return fromValue(returnValue);
    }

    /**
     * Saves the workbook to a new location.
     *
     * @param service  the {@link ExcelService} provided by the construct
     * @param file     a {@link File} object initialized to write to the new path
     * @param workbook the {@link XillWorkbook} which will be saved
     * @return a string pointing to the location where the file has been saved
     * @throws RobotRuntimeException when the file is read-only or cannot be written to
     */
    static MetaExpression processToFolder(ExcelService service, XillWorkbook workbook, File file) {
        XillWorkbook returnValue;
        try {
            returnValue = service.save(workbook, file);
        } catch (IllegalArgumentException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
        String savedString = "Saved [" + returnValue.getLocation() + "]";
        MetaExpression toReturn = fromValue(savedString);
        toReturn.storeMeta(returnValue);
        return toReturn;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                (a, b) -> process(service, context, a, b),
                new Argument("workbook", ATOMIC),
                new Argument("path", NULL, ATOMIC)
        );
    }

}
