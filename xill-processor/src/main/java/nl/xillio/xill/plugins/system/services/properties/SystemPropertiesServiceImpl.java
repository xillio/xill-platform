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

import com.google.inject.Singleton;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the default implementation of the {@link SystemPropertiesService}
 */
@Singleton
public class SystemPropertiesServiceImpl implements SystemPropertiesService {

    @Override
    public Map<String, Object> getProperties() {
        Map<Object, Object> properties = new LinkedHashMap<>();

        properties.putAll(System.getProperties());
        properties.putAll(System.getenv());

        return properties.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }

    @Override
    public String getProperty(final String key) {
        String result = System.getProperty(key);

        if (result == null) {
            result = System.getenv(key);
        }

        return result;
    }

}
