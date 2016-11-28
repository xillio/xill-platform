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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The exception class - fired when the operation failed.
 */
@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "Operation failed.")
public class XillOperationFailedException extends XillBaseException {
    public XillOperationFailedException(String message) {
        super(message);
    }

    public XillOperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
