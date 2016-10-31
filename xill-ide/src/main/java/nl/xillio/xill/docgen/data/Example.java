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
package nl.xillio.xill.docgen.data;

import nl.xillio.xill.docgen.PropertiesProvider;
import nl.xillio.xill.docgen.impl.ConstructDocumentationEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an example item of a {@link ConstructDocumentationEntity}.
 */
public class Example implements PropertiesProvider {
    private final List<ExampleNode> content = new ArrayList<>();
    private final String title;

    public Example(String title) {
        this.title = title;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("nodes", PropertiesProvider.extractContent(content));
        properties.put("title", title);
        return properties;
    }

    /**
     * Add a node to the example.
     *
     * @param node The node we want to add.
     */
    public void addContent(final ExampleNode node) {
        content.add(node);
    }

    /**
     * Returns all Nodes in the {@link Example}.
     *
     * @return all nodes in the example.
     */
    public List<ExampleNode> getExampleNodes() {
        return content;
    }
}
