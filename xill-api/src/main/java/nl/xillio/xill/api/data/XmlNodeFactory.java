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

/**
 * This interface is capable of building xml nodes from source.
 * @since 1.0.7
 */
public interface XmlNodeFactory {
	/**
	 * Creates XML document from string, parse it and returns root node (XML document)
	 *
	 * @param xmlText string that contains valid XMl document
	 * @return newly created XML node representing root node of the entire document
	 */
	XmlNode fromString(final String xmlText);
}
