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
package nl.xillio.plugins;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import me.biesaart.utils.Log;
import nl.xillio.xill.api.construct.Construct;
import org.apache.commons.lang3.text.WordUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;

/**
 * This class represents the base for all Xill plugins.
 */
public abstract class XillPlugin extends AbstractModule implements AutoCloseable {
    private static final Logger LOGGER = Log.get();
    private static final String LOAD_CONSTRUCTS_ERROR = "Can only load constructs in the loadConstructs() method.";
    private final List<Construct> constructs = new ArrayList<>();
    private final String defaultName;
    private boolean loadingConstructs = false;
    private String vendorUrl = null;
    @Inject
    private Injector injector;

    /**
     * Creates a new {@link XillPlugin} and sets the default name.
     */
    public XillPlugin() {
        // Set the default name
        String name = getClass().getSimpleName();
        String superName = XillPlugin.class.getSimpleName();
        if (name.endsWith(superName)) {
            name = name.substring(0, name.length() - superName.length());
        }
        defaultName = WordUtils.capitalize(name);
    }

    /**
     * Configures bindings for Injection.
     */
    @Override
    public void configure() {
    }

    /**
     * Gets a construct from this package.
     *
     * @param name the name of the construct
     * @return the construct or null if none was found for the provided name
     */
    public final Construct getConstruct(final String name) {
        Optional<Construct> result = getConstructs().stream().filter(c -> c.getName().equals(name)).findAny();

        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    /**
     * By default, the name of a {@link XillPlugin} is the concrete implementation name acquired using {@link Class#getSimpleName()}
     * without the {@link XillPlugin} suffix.
     *
     * @return the name of the package
     */
    public String getName() {
        return defaultName;
    }

    /**
     * Adds a construct to the package.
     *
     * @param construct the construct to add
     * @throws IllegalArgumentException when a construct with the same name already exists
     */
    protected final void add(final Construct construct) {
        if (!loadingConstructs) {
            throw new IllegalStateException(LOAD_CONSTRUCTS_ERROR);
        }
        if (getConstructs().stream().anyMatch(c -> c.getName().equals(construct.getName()))) {
            throw new IllegalArgumentException("A construct with the same name exsits.");
        }

        getConstructs().add(construct);
    }

    /**
     * Adds constructs to the package.
     *
     * @param constructs the constructs to add the the list
     * @throws IllegalArgumentException when a construct with the same name already exists
     */
    protected final void add(final Construct... constructs) {
        if (!loadingConstructs) {
            throw new IllegalStateException(LOAD_CONSTRUCTS_ERROR);
        }
        for (Construct c : constructs) {
            add(c);
        }
    }

    /**
     * Adds a list of constructs to the package.
     *
     * @param constructs the constructs to add to the list
     * @throws IllegalArgumentException when a construct with the same name already exists
     */
    protected final void add(final Collection<Construct> constructs) {
        if (!loadingConstructs) {
            throw new IllegalStateException(LOAD_CONSTRUCTS_ERROR);
        }
        constructs.forEach(this::add);
    }

    /**
     * Removes all constructs from this package and call {@link AutoCloseable#close()} on all constructs that implements it.
     */
    protected final void purge() {
        getConstructs().stream()
                .filter(construct -> construct instanceof AutoCloseable)
                .map(construct -> (AutoCloseable) construct)
                .forEach(construct -> {
                    try {
                        construct.close();
                    } catch (Exception e) {
                        LOGGER.error("Failed to close " + construct, e);
                    }
                });
        getConstructs().clear();
    }

    /**
     * Returns the version of the package.
     *
     * @return the version of the package
     */
    public Optional<String> getVersion() {
        if (getClass().isAnnotationPresent(PluginVersion.class)) {
            return Optional.of(getClass().getAnnotation(PluginVersion.class).value());
        }
        // No version override found. Defaulting to manifest value
        return Optional.ofNullable(getClass().getPackage().getImplementationVersion());
    }

    /**
     * Returns the vendor of the package.
     *
     * @return the vendor of the package
     */
    public Optional<String> getVendor() {
        if (getClass().isAnnotationPresent(PluginVendor.class)) {
            return Optional.of(getClass().getAnnotation(PluginVendor.class).value());
        }
        // No vendor override found. Defaulting to manifest value
        return Optional.ofNullable(getClass().getPackage().getImplementationVendor());
    }

    public Optional<String> getVendorUrl() {
        if (vendorUrl == null) {
            if (getClass().isAnnotationPresent(PluginVendor.class)) {
                String value = getClass().getAnnotation(PluginVendor.class).url();
                if (!value.isEmpty()) {
                    vendorUrl = value;
                }
            } else {
                // No vendor override found. Defaulting to manifest value
                vendorUrl = readFromManifest("Vendor-Url");
            }
        }
        return Optional.ofNullable(vendorUrl);
    }

    private String readFromManifest(String key) {
        if (getClass().getClassLoader() instanceof URLClassLoader) {
            URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            if (url != null) {
                try (InputStream stream = url.openStream()) {
                    Manifest manifest = new Manifest(stream);
                    return manifest.getMainAttributes().getValue(key);
                } catch (IOException e) {
                    LOGGER.error("Failed to get " + key + " from manifest", e);
                }
            }
        }
        return null;
    }

    @Override
    public void close() {
        purge();
    }

    /**
     * Loads all the constructs in the package.
     */
    public final void initialize() {
        loadingConstructs = true;
        loadConstructs();
        loadingConstructs = false;
    }

    /**
     * This is where the package can add all the constructs. If this method is not overridden it will load all constructs in the subpackage 'construct'.
     */
    public void loadConstructs() {

        new Reflections(getClass().getPackage().getName() + ".constructs", new ResourcesScanner(), getClass().getClassLoader())
                .getResources(name -> name.endsWith("Construct.class"))
                .stream()
                .map(resource -> resource.substring(0, resource.length() - ".class".length()).replace('/', '.'))
                .map(name -> {
                    try {
                        return getClass().getClassLoader().loadClass(name);
                    } catch (ClassNotFoundException e) {
                        LOGGER.debug("No class found for plugin: " + name, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(Construct.class::isAssignableFrom)
                .map(clazz -> (Class<Construct>) clazz)
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .map(injector::getInstance)
                .forEach(this::add);
    }

    /**
     * @return the constructs
     */
    public List<Construct> getConstructs() {
        return constructs;
    }
}
