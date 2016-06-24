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

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This {@link Instruction} represents the declaration of a custom function
 */
public class FunctionDeclaration extends Instruction {

    private final InstructionSet instructions;
    private final List<VariableDeclaration> parameters;

    /**
     * Create a new {@link FunctionDeclaration}
     *
     * @param instructions    The instructions associated with the function.
     * @param parameters      The function parameters.
     */
    public FunctionDeclaration(final InstructionSet instructions, final List<VariableDeclaration> parameters) {
        this.instructions = instructions;
        instructions.setParentInstruction(this);
        this.parameters = parameters;
        parameters.forEach(param -> param.setHostInstruction(instructions));
    }

    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) throws RobotRuntimeException {
        // Nothing to do on process
        // Actual functionality is in run method
        return InstructionFlow.doResume();
    }

    /**
     * Run the actual code.
     *
     * @param debugger     Contains all of the debugging information relevant for the function declaration.
     * @param arguments    The arguments passed to the robot.
     * @return The flow result
     * @throws RobotRuntimeException    Is thrown when an exception occurs during robot execution.
     */
    public InstructionFlow<MetaExpression> run(final Debugger debugger, final List<MetaExpression> arguments)
            throws RobotRuntimeException {

        // Initiate the parameters
        for (VariableDeclaration parameter : parameters) {
            parameter.process(debugger);
        }

        // Push the new arguments
        Iterator<MetaExpression> argumentItt = arguments.iterator();
        Iterator<VariableDeclaration> parametersItt = parameters.iterator();
        while (argumentItt.hasNext() && parametersItt.hasNext()) {
            MetaExpression expression = argumentItt.next();
            parametersItt.next().replaceVariable(expression);
        }

        debugger.startFunction(this);

        // Run the actual code
        InstructionFlow<MetaExpression> result = instructions.process(debugger);

        debugger.endFunction(this);
        if (result.hasValue()) {
            result.get().preventDisposal();

            parameters.forEach(VariableDeclaration::releaseVariable);

            result.get().allowDisposal();

            return InstructionFlow.doResume(result.get());
        } else {
            parameters.forEach(VariableDeclaration::releaseVariable);
            return InstructionFlow.doResume(ExpressionBuilderHelper.NULL);
        }

    }

    @Override
    public boolean preventDebugging() {
        // Debugger should not halt on function declarations
        return true;
    }

    @Override
    public Collection<Processable> getChildren() {
        List<Processable> children = new ArrayList<>(parameters);
        children.add(instructions);
        return children;
    }

    public int getParametersSize() {
        return parameters.size();
    }

}
