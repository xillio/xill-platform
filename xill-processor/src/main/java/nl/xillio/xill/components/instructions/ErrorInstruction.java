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
import nl.xillio.xill.api.components.InstructionFlow;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.debugging.ErrorBlockDebugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This {@link Instruction} represents the error mechanism
 */
public class ErrorInstruction extends CompoundInstruction {
    private final InstructionSet doInstructions;
    private final InstructionSet successInstructions;
    private final InstructionSet errorInstructions;
    private final InstructionSet finallyInstructions;
    private final VariableDeclaration cause;

    /**
     * Instantiate an {@link ErrorInstruction}
     *
     * @param doInstructions         A collection of instructions that need to be executed.
     * @param successInstructions    A collection of successfull instructions.
     * @param errorInstructions      A collection of erroneous instructions.
     * @param finallyInstructions    A collection of finally instructions.
     * @param cause                  Cause of the error.
     */
    public ErrorInstruction(InstructionSet doInstructions, InstructionSet successInstructions, InstructionSet errorInstructions, InstructionSet finallyInstructions, VariableDeclaration cause) {
        this.doInstructions = doInstructions;
        this.successInstructions = successInstructions;
        this.errorInstructions = errorInstructions;
        this.finallyInstructions = finallyInstructions;
        this.cause = cause;

        if (cause != null) {
            cause.setHostInstruction(errorInstructions);
        }

        if (doInstructions != null) {
            doInstructions.setParentInstruction(this);
        }

        if (successInstructions != null) {
            successInstructions.setParentInstruction(this);
        }

        if (errorInstructions != null) {
            errorInstructions.setParentInstruction(this);
        }

        if (finallyInstructions != null) {
            finallyInstructions.setParentInstruction(this);
        }
    }

    /**
     * start processing the blocks.
     *
     * @param debugger The debugger that should be used when processing this
     * @return the result flow
     */
    @Override
    public InstructionFlow<MetaExpression> process(final Debugger debugger) {
        ErrorBlockDebugger errorBlockDebugger = new ErrorBlockDebugger(debugger);
        return process(debugger, errorBlockDebugger);
    }

    public InstructionFlow<MetaExpression> process(final Debugger debugger, final ErrorBlockDebugger errorBlockDebugger) {

        InstructionFlow<MetaExpression> result = doInstructions.process(errorBlockDebugger);

        if (result.hasValue()) {
            result.get().preventDisposal();
        }

        if (errorBlockDebugger.hasError()) {
            processException(debugger, errorBlockDebugger);
        }

        processFinally(debugger, errorBlockDebugger.hasError());

        if (result.hasValue()) {
            result.get().allowDisposal();
        }

        if (errorBlockDebugger.hasError()) {
            return InstructionFlow.doResume();
        }

        return result;
    }

    /**
     * do the finally block.
     *
     * @param debugger the debugger
     */
    private void processFinally(Debugger debugger, boolean hadError) {
        //successBlock in finally because exceptions in these blocks should not be caught by errorBlockDebugger

        if (!hadError && successInstructions != null) {
            successInstructions.process(debugger);
        }

        if (finallyInstructions != null && finallyInstructions.process(debugger).hasValue()) {
            finallyInstructions.process(debugger);
        }
    }

    /**
     * process what happens if a exception is caught.
     *
     * @param debugger           the debugger
     * @param errorBlockDebugger the error debugger that holds an exception
     */
    private void processException(Debugger debugger, ErrorBlockDebugger errorBlockDebugger) {
        if (errorInstructions != null) {

            if (cause != null) {

                LinkedHashMap<String, MetaExpression> errorVar = new LinkedHashMap<>();
                errorVar.put("message", fromValue(getMessage(errorBlockDebugger.getError())));

                if (errorBlockDebugger.getErroredInstruction() != null) {
                    errorVar.put("line", fromValue(errorBlockDebugger.getErroredInstruction().getLineNumber()));
                    errorVar.put("robot", fromValue(errorBlockDebugger.getErroredInstruction().getRobotID().getURL().toString()));
                }

                cause.pushVariable(fromValue(errorVar), errorBlockDebugger.getStackDepth());
            }

            errorInstructions.process(debugger);
        }
    }

    private String getMessage(Throwable e) {
        return e.getMessage() == null ? "Unknown internal error" : e.getMessage();
    }

    @Override
    public Collection<Processable> getChildren() {
        List<Processable> children = new ArrayList<>();
        if (errorInstructions != null) {
            children.add(errorInstructions);
        }
        if (errorInstructions != null) {
            children.add(doInstructions);
        }
        if (errorInstructions != null) {
            children.add(finallyInstructions);
        }
        if (errorInstructions != null) {
            children.add(successInstructions);
        }
        if (errorInstructions != null) {
            children.add(cause);
        }
        return children;
    }

}
