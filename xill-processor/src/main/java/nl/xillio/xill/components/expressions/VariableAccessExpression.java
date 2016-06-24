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
package nl.xillio.xill.components.expressions;

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.components.instructions.VariableDeclaration;

import java.util.Arrays;
import java.util.Collection;

/**
 * This expression represents the accessing of a variable.
 */
public class VariableAccessExpression implements Processable {

    private final VariableDeclaration declaration;

    /**
     * Create a new {@link VariableAccessExpression} what will access provided
     * declaration.
     *
     * @param declaration    the provided declaration.
     */
    public VariableAccessExpression(final VariableDeclaration declaration) {
        this.declaration = declaration;
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        return InstructionFlow.doResume(declaration.getVariable());
    }

    @Override
    public Collection<Processable> getChildren() {
        return Arrays.asList(declaration);
    }
}
