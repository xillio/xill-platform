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

import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.Collection;

/**
 * This interface represents an object that can be processed.
 */
public interface Processable {

    /**
     * Processes this object.
     *
     * @param debugger The debugger that should be used when processing this
     * @return The return value is there is one
     * @throws RobotRuntimeException When processing went wrong
     */
    InstructionFlow<MetaExpression> process(Debugger debugger);

    /**
     * Collects all {@link Processable} used by this one. This is used to search through program trees.
     *
     * @return all Children
     */
    Collection<Processable> getChildren();
}
