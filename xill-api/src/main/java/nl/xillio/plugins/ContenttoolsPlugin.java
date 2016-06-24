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

import javafx.stage.Stage;
import nl.xillio.xill.api.XillEnvironment;

import java.util.function.Consumer;

/**
 * This interface represents the interface to the top-level Xill IDE plugins.
 */
public interface ContenttoolsPlugin {
    /**
     * Starts the plugin.
     *
     * @param stage the main stage of the UI
     * @param xill  the Xill entry point
     */
    void start(Stage stage, XillEnvironment xill);

    /**
     * Get the handler that will be called every
     * time a robot should be opened.
     *
     * @return the handler or null
     */
    Consumer<String> getSingleInstanceHandler();
}
