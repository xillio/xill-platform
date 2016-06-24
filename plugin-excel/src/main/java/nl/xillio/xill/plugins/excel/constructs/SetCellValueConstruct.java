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

/**
 * Construct to set a cell to a value.
 *
 * @author Geert Konijnendijk
 */
public class SetCellValueConstruct extends AbstractSetCellConstruct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                SetCellValueConstruct::process,
                new Argument(PARAMETER_NAME_SHEET, OBJECT),
                new Argument(PARAMETER_NAME_COLUMN, ATOMIC),
                new Argument(PARAMETER_NAME_ROW, ATOMIC),
                new Argument("value", ATOMIC));
    }

    static MetaExpression process(MetaExpression sheet, MetaExpression column, MetaExpression row, MetaExpression value) {
        return setCell(sheet, column, row, value, FALSE);
    }
}
