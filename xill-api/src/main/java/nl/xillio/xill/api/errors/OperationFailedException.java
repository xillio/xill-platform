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
package nl.xillio.xill.api.errors;

/**
 * The specific exception whenever a certain operation could not be performed.
 */
public class OperationFailedException extends RobotRuntimeException {

    protected final String reason;
    protected final String operation;

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param operation the operation that failed (imperative form without explicit casing)
     * @param reason what went wrong (full human readable sentence)
     * @param advice next course of action to solve the problem (human readable)
     * @param cause the cause
     */
    public OperationFailedException(final String operation, final String reason, final String advice, final Throwable cause) {
        super("Could not " + operation + ".\n\n== Reason ==\n" + reason + (advice == null ? "" : " " + advice), cause);
        this.reason = reason;
        this.operation = operation;
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param operation the operation that failed (imperative form without explicit casing)
     * @param reason what went wrong (full human readable sentence)
     * @param advice next course of action to solve the problem (human readable)
     */
    public OperationFailedException(final String operation, final String reason, final String advice) {
        this(operation, reason, advice, null);
    }

    /**
     * Constructs a new exception with the specified detail message and cause without advice.
     *
     * @param operation the operation that failed (imperative form without explicit casing)
     * @param reason what went wrong (full human readable sentence)
     * @param cause the cause
     */
    public OperationFailedException(final String operation, final String reason, final Throwable cause) {
        this(operation, reason, null, cause);
    }

    /**
     * Constructs a new exception with the specified detail message without advice.
     *
     * @param operation the operation that failed (imperative form without explicit casing)
     * @param reason what went wrong (full human readable sentence)
     */
    public OperationFailedException(final String operation, final String reason) {
        this(operation, reason, null, null);
    }

    @Override
    public String getShortMessage() {
        return "Could not " + operation + ". " + reason;
    }
}