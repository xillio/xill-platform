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
import nl.xillio.xill.plugins.codec.decode.services.DecoderService;

import java.util.*;

/**
 * This class represents a parameter in a {@link ConstructDocumentationEntity}.
 */
public class ParDescription implements PropertiesProvider {
    private String parameterName = null;
    private String description = null;

    /**
     * The constructor for the parDescription which sets the parameter and the description.
     *
     * @param parameterName the name of parameter this description describes.
     * @param description   the description of this parameter
     * @throws NullPointerException when the parameter  is null
     */
    public ParDescription(String parameterName, String description) {
        if (parameterName == null) {
            throw new NullPointerException("Parameters must have names");
        }
        this.parameterName = parameterName;
        this.description = description;
    }

    @Override
    public Map<String, Object> getProperties() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("parameterName", parameterName);
        map.put("parameterDescription", description);
        return map;
    }

    /**
     * Set the description of the parameter
     *
     * @param description the description of the parameter
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set the paramter that this describes
     *
     * @param parameterName the name of the described parameter
     */
    public void setParameter(String parameterName) {
        this.parameterName = parameterName;
    }

}
