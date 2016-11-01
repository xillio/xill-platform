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
package nl.xillio.xill.docgen.impl;

import nl.xillio.xill.docgen.DocumentationEntity;
import nl.xillio.xill.docgen.PropertiesProvider;

import java.util.*;

/**
 * This class represents the documentation of a package
 *
 * @author Thomas Biesaart
 * @author Ivor van der Hoog
 * @since 12-8-2015
 */
public class PackageDocumentationEntity implements DocumentationEntity {

    private String name;
    private final List<ConstructDocumentationEntity> children = new ArrayList<>();


    @Override
    public String getIdentity() {
        return name;
    }

    @Override
    public String getType() {
        return "package";
    }

    @Override
    public List<String> getTags() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getProperties() {
        children.sort(DocumentationEntity.SORT_BY_IDENTITY);
        Map<String, Object> properties = new HashMap<>();
        properties.put("packageName", name);
        properties.put("constructs", PropertiesProvider.extractContent(children));
        return properties;
    }

    public void add(DocumentationEntity entity) {
        children.add((ConstructDocumentationEntity) entity);
    }

    public void setName(String name) {
        this.name = name;
    }
}
