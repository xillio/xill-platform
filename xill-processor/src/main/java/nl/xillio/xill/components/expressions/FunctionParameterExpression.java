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

import nl.xillio.xill.api.components.Processable;
import nl.xillio.xill.components.instructions.FunctionDeclaration;

/**
 * This interface represents an Expression that takes a function as a parameter
 */
public interface FunctionParameterExpression extends Processable {
    /**
     * Set the functional parameter
     *
     * @param function  the value to which the function declaration needs to be set.
     */
    public void setFunction(FunctionDeclaration function);
}
