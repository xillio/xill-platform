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

import me.biesaart.utils.StringUtils;
import nl.xillio.xill.docgen.PropertiesProvider;
import nl.xillio.xill.docgen.impl.ConstructDocumentationEntity;

import java.util.*;

/**
 * This class represents a parameter in a {@link ConstructDocumentationEntity}.
 */
public class Parameter implements PropertiesProvider {
    private String name = null;
    private String defaultValue = null;
    private final List<String> types = new ArrayList<>();

    /**
     * The constructor for the parameter which sets the types and the name.
     *
     * @param types the comma separated types of this parameter or null for any
     * @param name  the name of this parameter
     * @throws NullPointerException when the name is null
     */
    public Parameter(String types, String name) {
        if (name == null) {
            throw new NullPointerException("Parameters must have names");
        }
        setType(types);
        this.name = name;
    }

    @Override
    public Map<String, Object> getProperties() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("defaultValue", defaultValue);
        map.put("types", types);
        map.put("type", StringUtils.join(types, ", "));
        return map;
    }

    /**
     * Set the name of the parameter.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return The name of the parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Add all parameter types.
     * We default to ANY if there are no types given.
     *
     * @param types The parameter types as String.
     */
    public void setType(String types) {
        if (types != null) {
            String[] theseTypes = types.toUpperCase().split("\\s+,");
            Collections.addAll(this.types, theseTypes);
        } else {
            this.types.add("ANY");
        }
    }

    /**
     * Set the default value for the parameter.
     *
     * @param defaultValue The default value.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
