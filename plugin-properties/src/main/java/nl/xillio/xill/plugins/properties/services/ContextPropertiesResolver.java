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
        resolver.register("xill.projectPath", context -> context.getRootRobot().getProjectPath().getAbsolutePath());
        resolver.register("xill.robotPath", context -> context.getRootRobot().getPath().getAbsolutePath());
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
