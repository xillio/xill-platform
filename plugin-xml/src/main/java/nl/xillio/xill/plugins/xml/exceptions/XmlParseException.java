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
package nl.xillio.xill.plugins.xml.exceptions;

/**
 * An exception to throw when XML parsing error occurs
 *
 * @author Zbynek Hochmann
 */
public class XmlParseException extends Exception {
    private static final long serialVersionUID = 14589742606197714L;

    /**
     * @param message error message
     */
    public XmlParseException(String message) {
        super(message);
    }

    /**
     * @param message error message
     * @param cause   the cause
     */
    public XmlParseException(String message, Exception cause) {
        super(message, cause);
    }
}


