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
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;

/**
 * Construct to return the current amount of rows in a sheet.
 *
 * @author Daan Knoope
 */
public class RowCountConstruct extends AbstractExcelConstruct {

    /**
     * Returns the number of rows in the provided sheet.
     *
     * @param sheet the {@link XillSheet} which' rows need to be counted
     * @return a {@link MetaExpression} containing the number of rows in the sheet
     */
    static MetaExpression process(MetaExpression sheet) {
        XillSheet tempSheet = assertMeta(sheet, "parameter 'sheet'", XillSheet.class, "result of loadSheet or createSheet");
        return fromValue(tempSheet.getRowLength());
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(RowCountConstruct::process, new Argument(PARAMETER_NAME_SHEET, OBJECT));
    }

}
