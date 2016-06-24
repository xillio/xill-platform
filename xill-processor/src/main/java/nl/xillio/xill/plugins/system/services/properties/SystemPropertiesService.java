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
package nl.xillio.xill.plugins.system.services.properties;

import com.google.inject.ImplementedBy;
import nl.xillio.xill.services.PropertiesProvider;

/**
 * This {@link PropertiesProvider} can provide system properties
 */
@ImplementedBy(SystemPropertiesServiceImpl.class)
public interface SystemPropertiesService extends PropertiesProvider {

    /**
     * Get a single property
     *
     * @param key the property key
     * @return the property value or null
     */
    public String getProperty(String key);

}
