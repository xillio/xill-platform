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
package nl.xillio.xill.plugins.xml.services;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.plugins.xml.XMLXillPlugin;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * This interface represents some of the operations for the {@link XMLXillPlugin}.
 *
 * @author Zbynek Hochmann
 */

@ImplementedBy(XsdServiceImpl.class)
public interface XsdService {
    /**
     * Verifies if XML file is valid according to XSD specification
     *
     * @param xmlFilePath XML file path document
     * @param xsdFilePath XSD file path
     * @param logger      CT logger
     * @return true if XML file is valid, otherwise false
     */
    boolean xsdCheck(final Path xmlFilePath, final Path xsdFilePath, final Logger logger);
}
