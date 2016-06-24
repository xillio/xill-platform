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
package nl.xillio.xill.api.data;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This interface represents an object that contains XML information.
 *
 * @author Zbynek Hochmann
 * @since 3.0.0
 */
public interface XmlNode extends MetadataExpression {
	/**
	 * Returns XML document of this node
	 *
	 * @return {@link org.w3c.dom.Document} of this node
	 */
	Document getDocument();

	/**
	 * @return {@link org.w3c.dom.Node} data specifying this node
	 */
	Node getNode();

	/**
	 * Returns XML content of this node in string format
	 *
	 * @return XML content in string format
	 */
	String getXmlContent();

	/**
	 * @return a string containing all text extracted from XML node or XML document
	 */
	String getText();
}
