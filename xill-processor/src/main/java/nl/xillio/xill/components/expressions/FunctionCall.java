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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.components.instructions.FunctionDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a call of a custom function
 */
public class FunctionCall implements Processable {

    private FunctionDeclaration function;
    private List<Processable> arguments;

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {

        // Process arguments
        List<MetaExpression> expressions = new ArrayList<>();

        for (Processable argument : arguments) {
            expressions.add(argument.process(debugger).get());
        }

        return function.run(debugger, expressions);

    }

    /**
     * Initialize this {@link FunctionCall}
     *
     * @param function     the function declaration.
     * @param arguments    the arguments of the function.
     */
    public void initialize(final FunctionDeclaration function, final List<Processable> arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public Collection<Processable> getChildren() {
        List<Processable> children = new ArrayList<>(arguments);
        children.add(function);
        return children;
    }

    public FunctionDeclaration getDeclaration() {
        return function;
    }
}
