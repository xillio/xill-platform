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
package nl.xillio.xill.versioncontrol;

/**
 * Created by Dwight.Peters on 29-Nov-16.
 */
public class GitException extends Exception {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *            detail message
     * @param cause
     *            cause
     * @since 3.1
     */
    protected GitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and no
     * cause.
     *
     * @param message
     *            detail message
     * @since 3.1
     */
    protected GitException(String message) {
        super(message);
    }
}
