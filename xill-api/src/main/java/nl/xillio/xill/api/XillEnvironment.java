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
package nl.xillio.xill.api;

import com.google.inject.Injector;
import nl.xillio.plugins.PluginLoadFailure;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.api.io.ResourceLoader;
import nl.xillio.xill.services.ProgressTracker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * This interface represents an object that can assemble a {@link XillProcessor}.
 *
 * @author Thomas Biesaart
 */
public interface XillEnvironment extends AutoCloseable {

    /**
     * The default file extension for robot files.
     */
    String ROBOT_EXTENSION = ".xill";

    /**
     * The default file extension for robot templates.
     */
    String ROBOT_TEMPLATE_EXTENSION = ".xilt";

    /**
     * Enables or disables loading plugins from the user's home folder.
     *
     * @param value whether plugins from home folder should be loaded
     * @return self
     */
    XillEnvironment setLoadHomeFolder(boolean value);

    /**
     * Adds a folder to the plugin loading queue.
     *
     * @param path the path to the folder
     * @return self
     * @throws IOException if the provided path is not a folder, doesn't exist or is inaccessible.
     */
    XillEnvironment addFolder(Path path) throws IOException;

    /**
     * Sets the root injector for the plugin environment.
     *
     * @param injector the injector
     * @return self
     */
    XillEnvironment setRootInjector(Injector injector);

    /**
     * Sets the {@link XillThreadFactory} used to create threads in plugins.
     * <p>
     * Needs to be called before {@link #loadPlugins()}.
     *
     * @param xillThreadFactory the factory
     * @return self
     */
    XillEnvironment setXillThreadFactory(XillThreadFactory xillThreadFactory);

    /**
     * Loads plugins from the added folders.
     *
     * @return self
     * @throws IOException if we could not read the plugins
     * @see XillEnvironment#addFolder(Path)
     */
    XillEnvironment loadPlugins() throws IOException;

    /**
     * Builds a processor for a specific execution.
     *
     * To resolve robots a resource loader is created. This loader builds a chain of paths (directories and archives)
     * that are traversed when searching for robots. If a Path in this chain is recognised to be a file, it is
     * interpreted as an archive. Otherwise it is interpreted as a directory.
     *
     * @param workingDirectory   the working directory of the project
     * @param fullyQualifiedName the fully qualified name of the robot that should be started
     * @param robotPath the robot path
     * @return the processor
     * @throws IOException if we could not load the processor
     */
    XillProcessor buildProcessor(Path workingDirectory, String fullyQualifiedName, Path... robotPath) throws IOException;

    /**
     * Builds a processor for a specific execution, with a debugger.
     *
     * To resolve robots a resource loader is created. This loader builds a chain of paths (directories and archives)
     * that are traversed when searching for robots. If a Path in this chain is recognised to be a file, it is
     * interpreted as an archive. Otherwise it is interpreted as a directory.
     *
     * @param workingDirectory   the working directory of the project
     * @param fullyQualifiedName the fully qualified name of the robot that should be started
     * @param debugger           the debugger that should be used
     * @return the processor
     * @throws IOException if we could not load the processor
     */
    XillProcessor buildProcessor(Path workingDirectory, String fullyQualifiedName, Debugger debugger, Path... robotPath) throws IOException;

    /**
     * Gets a list of all loaded plugins.
     *
     * @return all loaded plugins.
     */
    List<XillPlugin> getPlugins();

    /**
     * Gets a list of found plugins that could not be loaded due to licensing issues.
     *
     * @return all rejected plugins
     */
    default List<PluginLoadFailure> getMissingLicensePlugins() {
        return Collections.emptyList();
    }

    /**
     * Gets a {@link ProgressTracker} object.
     *
     * @return ProgressTracker object
     */
    ProgressTracker getProgressTracker();

    /**
     * Close all loaded plugins.
     */
    @Override
    void close();
}
