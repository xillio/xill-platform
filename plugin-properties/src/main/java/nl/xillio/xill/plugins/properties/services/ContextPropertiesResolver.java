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
package nl.xillio.xill.plugins.properties.services;

import nl.xillio.xill.api.construct.ConstructContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * This service is responsible for resolving properties from the {@link ConstructContext}.
 *
 * @author Thomas Biesaart
 */
public class ContextPropertiesResolver {
    private final Map<String, Function<ConstructContext, String>> propertiesProviders = new HashMap<>();

    public static ContextPropertiesResolver defaultXillResolver() {
        ContextPropertiesResolver resolver = new ContextPropertiesResolver();
        resolver.register("xill.workingDirectory", context -> context.getWorkingDirectory().toString());
        resolver.register("xill.robotUrl", context -> context.getRobotID().getURL().toString());
        resolver.register("xill.robotPath", context -> context.getRobotID().getResourcePath());
        return resolver;
    }

    public Optional<String> resolve(String key, ConstructContext context) {
        if (!propertiesProviders.containsKey(key)) {
            return Optional.empty();
        }

        Function<ConstructContext, String> function = propertiesProviders.get(key);
        return function.andThen(Optional::ofNullable).apply(context);
    }

    void register(String key, Function<ConstructContext, String> provider) {
        Objects.requireNonNull(provider);
        propertiesProviders.put(key, provider);
    }
}
