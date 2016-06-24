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
package nl.xillio.xill.plugins.web;

import java.io.IOException;

/**
 * Thrown to indicate that the URL could not be found or is invalid.
 */
public class InvalidUrlException extends IOException {
    /**
     * Constructs a {@code MalformedURLException} with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidUrlException(String msg) {
        super(msg);
    }
}
