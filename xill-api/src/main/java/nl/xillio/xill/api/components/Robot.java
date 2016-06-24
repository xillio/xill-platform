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

import nl.xillio.xill.api.errors.RobotRuntimeException;

import java.util.List;
import java.util.UUID;

/**
 * This interface represents a Robot.
 */
public interface Robot extends InstructionSet {

    /**
     * This method uses a BFS algorithm to find a target among the children.
     *
     * @param target the target to calculate the path to from the root
     * @return the path to the target or an empty list if the target wasn't found.
     */
    List<Processable> pathToInstruction(Instruction target);

    /**
     * Initializes the robot to be used as a library.
     *
     * @param skipSelf set this to true to skip initializing self and only initialize children
     * @throws RobotRuntimeException when the library couldn't be initialized
     */
    void initializeAsLibrary(boolean skipSelf) throws RobotRuntimeException;

    /**
     * Sets the argument for this robot. This is used by the callbot component of the language.
     *
     * @param expression the value to set for the argument expression
     */
    void setArgument(MetaExpression expression);

    /**
     * @return the argument which was set for this robot
     */
    MetaExpression getArgument();

    /**
     * @return whether an argument has been set for this robot
     */
    boolean hasArgument();

    UUID getCompilerSerialId();
}
