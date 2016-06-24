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
package nl.xillio.xill.components.instructions;

import nl.xillio.xill.CodePosition;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.*;

/**
 * This is a {@link VariableDeclaration}, but with adjusted behaviours specific to function parameters. 
 *
 * @author Geert Konijnendijk
 */
public class FunctionParameterDeclaration extends VariableDeclaration{
    public FunctionParameterDeclaration(Processable expression, String name, Robot robot) {
        super(expression, name, robot);
    }

    public FunctionParameterDeclaration(Processable expression, String name) {
        super(expression, name);
    }

    @Override
    protected int getInsertionIndex(Debugger debugger) {
        // Function parameters are assigned on the stack frame above the function, but should be inserted in the function
        return debugger.getStackDepth() + 1;
    }

    /**
     * A variable declared to be null.
     *
     * @param position The position in code where the null variable occurs.
     * @param name     The name of the variable that is declared to be null.
     * @return A declaration with value {@link ExpressionBuilder#NULL}
     */
    public static FunctionParameterDeclaration nullDeclaration(final CodePosition position, final String name) {
        FunctionParameterDeclaration dec = new FunctionParameterDeclaration(ExpressionBuilderHelper.NULL, name);
        dec.setPosition(position);

        return dec;
    }
}
