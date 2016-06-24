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
package nl.xillio.xill.api.components;

import java.util.NoSuchElementException;

/**
 * This object represents a break in the instruction flow.
 *
 * @param <T> the type contained in this instruction flow
 */
public class InstructionFlow<T> {
    private enum Type {
        CONTINUE, BREAK, RETURN, RESUME
    }

    private final Type flowType;
    private final T value;

    private InstructionFlow(final Type flowType, final T value) {
        this.flowType = flowType;
        this.value = value;
    }

    /**
     * @return whether this is a return flow
     */
    public boolean returns() {
        return flowType == Type.RETURN;
    }

    /**
     * @return whether the flow should be interrupted
     */
    public boolean breaks() {
        return returns() || flowType == Type.BREAK;
    }

    /**
     * @return whether the current instruction should be skipped
     */
    public boolean skips() {
        return flowType == Type.CONTINUE;
    }

    /**
     * @return whether the flow was not interrupted
     */
    public boolean resumes() {
        return flowType == Type.RESUME;
    }

    /**
     * @return whether a non-null value is set
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * Retrieves the stored value.
     *
     * @return the value
     * @throws NoSuchElementException When no value was set
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value was set.");
        }
        return value;
    }

    /**
     * Creates a new instruction flow that represents a return value.
     *
     * @param <T>   the type parameter for instruction flow
     * @param value the value to set
     * @return the instruction flow
     */
    public static <T> InstructionFlow<T> doReturn(final T value) {
        return new InstructionFlow<T>(Type.RETURN, value);
    }

    /**
     * Creates a new instruction flow that represents an empty return.
     *
     * @param <T> the type parameter for instruction flow
     * @return the instruction flow
     */
    public static <T> InstructionFlow<T> doReturn() {
        return new InstructionFlow<T>(Type.RETURN, null);
    }

    /**
     * @param <T> the type parameter for instruction flow
     * @return a continue token
     */
    public static <T> InstructionFlow<T> doContinue() {
        return new InstructionFlow<T>(Type.CONTINUE, null);
    }

    /**
     * @param <T> the type parameter for instruction flow
     * @return a break token
     */
    public static <T> InstructionFlow<T> doBreak() {
        return new InstructionFlow<T>(Type.BREAK, null);
    }

    /**
     * @param <T> the type parameter for instruction flow
     * @return a resume token
     */
    public static <T> InstructionFlow<T> doResume() {
        return new InstructionFlow<T>(Type.RESUME, null);
    }

    /**
     * @param <T>   the type parameter for instruction flow
     * @param value the value to set
     * @return a resume token with a value
     */
    public static <T> InstructionFlow<T> doResume(final T value) {
        return new InstructionFlow<T>(Type.RESUME, value);
    }
}
