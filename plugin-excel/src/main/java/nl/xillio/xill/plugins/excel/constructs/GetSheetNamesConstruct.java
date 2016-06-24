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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Construct to get the name of all the sheets in the provided workbook.
 *
 * @author Daan Knoope
 */
public class GetSheetNamesConstruct extends Construct {

    /**
     * Processes the xill code to return the name of the sheets in the provided workbook.
     *
     * @param workbookInput a workbook object as created by
     *                      {@link CreateWorkbookConstruct} or {@link LoadWorkbookConstruct}
     * @return a list of the name of all the sheets in the workbook provided
     * @throws RobotRuntimeException when a wrong {@link XillWorkbook} (null)
     *                               has been provided
     */
    static MetaExpression process(MetaExpression workbookInput) {
        XillWorkbook workbook = assertMeta(workbookInput, "parameter 'workbook'",
                XillWorkbook.class, "result of loadWorkbook or createWorkbook");
        List<String> workbookNames = workbook.getSheetNames();
        List<MetaExpression> toReturn = workbookNames.stream().map(ExpressionBuilderHelper::fromValue).collect(Collectors.toList());
        return fromValue(toReturn);
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                GetSheetNamesConstruct::process,
                new Argument("workbook", ATOMIC));
    }

}
