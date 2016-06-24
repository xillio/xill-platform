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
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.plugins.xml.XMLXillPlugin;

import java.util.List;
import java.util.Map;

/**
 * This interface represents some of the operations for the {@link XMLXillPlugin}.
 *
 * @author Zbynek Hochmann
 */

@ImplementedBy(XpathServiceImpl.class)
public interface XpathService {
    /**
     * Selects node(s) from XML document using XPath locator
     *
     * @param node       XML node
     * @param xpathQuery XPath locator specification
     * @param namespaces optional associative array containing namespace definitions
     * @return list of selected XML nodes
     */
    List<Object> xpath(final XmlNode node, final String xpathQuery, final Map<String, String> namespaces);
}
