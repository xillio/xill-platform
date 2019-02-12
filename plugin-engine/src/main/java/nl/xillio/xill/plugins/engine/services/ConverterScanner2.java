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
package nl.xillio.xill.plugins.engine.services;

import com.google.inject.Injector;
import nl.xillio.engine.configuration.Configuration;
import nl.xillio.engine.model.converter.Converter;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service uses reflection to automatically find all implementations of the Converter interface
 * based on a given configuration class.
 */
@Singleton
public class ConverterScanner2 extends nl.xillio.engine.services.ConverterScanner {
    private static final String SHARED_CONVERTER_PACKAGE = "nl.xillio.engine.model.converter.shared";
    private static final String BASE_CONVERTER_PACKAGE = "nl.xillio.engine.model.converter";
    private final Injector injector;

    @Inject
    public ConverterScanner2(Injector injector) {
        super(injector);
        this.injector = injector;
    }

    /**
     * Scan for converters based on a configuration class.
     *
     * @param configurationClass The configuration class that corresponds to the connector.
     * @param <T>                The type of the configuration class.
     * @return A list of converters.
     */
    public <T extends Configuration> List<Converter<T>> scanConverters(Class<T> configurationClass) {

        // First iterate over the connector converter package, then the shared converter package
        return Stream.of(configurationClass.getPackage().getName(), SHARED_CONVERTER_PACKAGE, BASE_CONVERTER_PACKAGE)
                // Create a reflection for each package
                .map(pack -> new Reflections(pack, SHARED_CONVERTER_PACKAGE, BASE_CONVERTER_PACKAGE))
                // And map those to all subclasses of converters
                .flatMap(reflections -> reflections.getSubTypesOf(Converter.class).stream())
                // Only keep concrete implementations
                .filter(clazz -> !Modifier.isInterface(clazz.getModifiers()))
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .filter(clazz -> clazz.getEnclosingClass() == null || Modifier.isStatic(clazz.getModifiers()))
                // And instantiate those using injection
                .map(injector::getInstance)
                .map(converter -> (Converter<T>) converter)
                .collect(Collectors.toList());
    }
}
