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
package nl.xillio.xill.webservice.exceptions;

import nl.xillio.xill.webservice.model.Runtime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when {@link Runtime} fails
 * to compile a robot.
 *
 * @author Geert Konijnendijk
 */
@ResponseStatus(code = HttpStatus.CONFLICT, reason = "The robot does not compile")
public class CompileException extends BaseException {

    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
