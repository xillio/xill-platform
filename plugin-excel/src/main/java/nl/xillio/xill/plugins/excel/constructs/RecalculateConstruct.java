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
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.apache.poi.ss.formula.functions.NotImplementedFunction;

import java.io.File;
import java.io.IOException;

/**
 * Construct to recalculate the current workbook.
 *
 * @author Paul van der Zandt
 */
public class RecalculateConstruct extends Construct {

    @Inject
    private ExcelService service;

    /**
     * Processes the xill code to recalculate the provided workbook.
     *
     * @param service       the {@link ExcelService} provided by the construct
     * @param context       the {@link ConstructContext} provided by the construct
     * @param workbookInput a workbook object created by {@link CreateWorkbookConstruct} or
     *                      {@link LoadWorkbookConstruct} containing a {@link XillWorkbook}
     * @return TRUE if successfull
     * @throws RobotRuntimeException when the file is read-only or cannot be written to
     */
    static MetaExpression process(ExcelService service, ConstructContext context, MetaExpression workbookInput) {
        XillWorkbook workbook = assertMeta(workbookInput, "parameter 'workbook'", XillWorkbook.class, "result of loadWorkbook or createWorkbook");
        return process(workbook);
    }

    /**
     * Recalculate the workbook and handle exceptions.
     *
     * @param workbook the {@link XillWorkbook} which will be recalculated
     * @return TRUE if successfull
     * @throws RobotRuntimeException when the workbook can not be recalculated
     */
    static MetaExpression process(XillWorkbook workbook) {
        try {
            workbook.recalculate();
        } catch (NotImplementedException nie) {
            Throwable theCause = nie.getCause();
            if (theCause instanceof NotImplementedFunctionException) {
                NotImplementedFunctionException cause = (NotImplementedFunctionException) theCause;
                String message = String.format("Workbook contains unknown function '%s' in cell %s",
                        cause.getFunctionName(),
                        nie.getMessage().substring(22));
                throw new RobotRuntimeException(message);
            } else {
                throw new RobotRuntimeException(nie.getMessage(), nie);
            }
        } catch (RuntimeException e) {
            throw new RobotRuntimeException(e.getMessage(), e);
        }
        return TRUE;
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                a -> process(service, context, a),
                new Argument("workbook", ATOMIC)
        );
    }

}
