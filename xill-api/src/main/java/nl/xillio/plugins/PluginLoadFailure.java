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

import org.apache.commons.lang3.StringUtils;

/**
 * Container class for handling exception thrown during loading plugins.
 */
public final class PluginLoadFailure {
    private String moduleName;
    private String className;
    private String cause;

    private PluginLoadFailure(String moduleName, String className, String cause) {
        this.moduleName = moduleName;
        this.className = className;
        this.cause = cause;
    }

    /**
     * Parse a PluginLoadFailure object from a string. The message should be set up using 3 parts split by ':'.
     * <ol>
     * <li>Name of the module/plugin</li>
     * <li>Class name of the plugin</li>
     * <li>Cause of the exception</li>
     * </ol>
     * Extra elements that could occur, because there are more ':' in the message, are added to the cause string.<br>
     * If the message can not be split into 3 parts, then the returned PluginLoadFailure has:<br>
     * moduleName = <code>"COMMON"</code><br>
     * className = <code>emtpy ("")</code><br>
     * cause = <code>whole message string</code><br>
     *
     * @param message
     * @return
     */
    public static PluginLoadFailure parse(String message) {
        String moduleName;
        String className;
        String cause;
        String[] parts = StringUtils.split(message, ":", 3);
        if (parts.length < 3) {
            moduleName = "COMMON";
            className = "";
            cause = message;
        } else {
            moduleName = parts[0];
            className = parts[1];
            cause = parts[2];
        }
        return new PluginLoadFailure(moduleName, className, cause);
    }

    /**
     * Gets the name of the module that could not be loaded.
     *
     * @return a name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Gets the name of class that loads the plugin.
     *
     * @return a class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets a descriptive message of what went wrong during the loading of the plugin.
     *
     * @return a message
     */
    public String getCause() {
        return cause;
    }
}
