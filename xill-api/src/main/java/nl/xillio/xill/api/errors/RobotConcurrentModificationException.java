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

import java.util.ConcurrentModificationException;

/**
 * Wrapper exception for ConcurrentModificationException
 */
public class RobotConcurrentModificationException extends OperationFailedException {

    /**
     * Constructs a new exception based on the provided {@link ConcurrentModificationException}
     * @param e the original {@link ConcurrentModificationException}
     */
    public RobotConcurrentModificationException(ConcurrentModificationException e){
        super("access expression", "Variable is being both written to and being read by separate threads.", "Please don't modify variables " +
                "that are being used by other threads.", e.getCause());

    }

}
