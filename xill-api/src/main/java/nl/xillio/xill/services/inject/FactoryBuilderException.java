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
package nl.xillio.xill.services.inject;

/**
 * This exception is generally thrown when a {@link Factory} fails to create an instance using {@link Factory#get()}.
 */
public class FactoryBuilderException extends RuntimeException {

    private static final long serialVersionUID = -2311064385511102301L;

    /**
     * Constructs a new runtime exception with the specified detail message and cause.
     *
     * @param message the message
     * @param e       the cause
     */
    public FactoryBuilderException(String message, Throwable e) {
        super(message, e);
    }


}
