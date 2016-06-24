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
package nl.xillio.xill.util.settings;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that contains common methods for using simple settings
 * There are simple variable category and the name and value of the simple variable including possibility of default value and encrypting of the variable value; and description text.
 *
 * @author Zbynek Hochmann
 */
public class SimpleVariableHandler {

    private static final Logger LOGGER = Log.get();

    private static final String NAME = "name";
    private static final String KEYNAME = NAME;
    private static final String VALUE = "value";
    private static final String DEFAULT = "default";
    private static final String DESCRIPTION = "description";
    private static final String ENCRYPTED = "encrypted";

    private ContentHandler content;

    SimpleVariableHandler(ContentHandler content) {// Can be instantiated within package only
        this.content = content;
    }

    /**
     * Sets the new string value to named simple variable in provided category
     * The simple variable must be registered before otherwise it will cause error.
     *
     * @param category The name of category
     * @param name     The name of variable
     * @param value    The value of variable
     */
    public void save(final String category, final String name, final String value) {
        this.save(category, name, value, false);
    }

    /**
     * Sets the new boolean value to named simple variable in provided category
     * The simple variable must be registered before otherwise it will cause error.
     *
     * @param category The name of category
     * @param name     The name of variable
     * @param value    The value of variable
     */
    public void save(final String category, final String name, final boolean value) {
        this.save(category, name, value ? "true" : "false");
    }

    /**
     * Sets the new value to named simple variable in provided category
     * If simple variable has not been registered before and allowUnregistered=false, it will cause error.
     * If simple variable has not been registered before and allowUnregistered=true, it will always set the new value (but just the value, nothing more)
     *
     * @param category          The name of category
     * @param name              The name of variable
     * @param value             The value of variable
     * @param allowUnregistered true if the simple variable can be stored without prior registration
     */
    public void save(final String category, final String name, final String value, final boolean allowUnregistered) {
        try {
            HashMap<String, Object> itemContent = new HashMap<>();
            String valueStr = value;

            if (!this.content.exist(category, KEYNAME, name)) {
                // item does not exist!
                if (allowUnregistered) {
                    // need to add name (as a key)
                    itemContent.put(NAME, name);
                } else {
                    throw new IllegalArgumentException(String.format("Settings [%1$s] has not been registered", name));
                }
            } else {
                Object o = this.content.get(category, name).get(ENCRYPTED);
                if ((o != null) && (o.toString().equals("1"))) {
                    valueStr = ContentHandlerImpl.encrypt(value);
                }
            }

            itemContent.put(VALUE, valueStr);

            this.content.set(category, itemContent, KEYNAME, name);
        } catch (IOException e) {
            LOGGER.error("Cannot save simple settings variable.", e);
        }
    }

    /**
     * Set basic definition of simple variable
     *
     * @param category     The category name (e.g. "Layout")
     * @param name         The name of simple variable (e.g. "LeftPanelWidth")
     * @param defaultValue The default value that is use in case of the value of the variable is not set yet
     * @param description  The description of the variable
     * @param encrypted    True if the variable value is stored encrypted
     */
    public void register(final String category, final String name, final String defaultValue, final String description, final boolean encrypted) {
        try {
            HashMap<String, Object> itemContent = new HashMap<>();
            itemContent.put(NAME, name);
            itemContent.put(DEFAULT, defaultValue);
            itemContent.put(DESCRIPTION, description);
            itemContent.put(ENCRYPTED, encrypted ? "1" : "0");

            this.content.set(category, itemContent, KEYNAME, name);
        } catch (Exception e) {
            LOGGER.error("Cannot register simple settings variable!", e);
        }
    }

    /**
     * Set basic definition of simple variable (not encrypted)
     *
     * @param category     The category name (e.g. "Layout")
     * @param name         The name of simple variable (e.g. "LeftPanelWidth")
     * @param defaultValue The default value that is use in case of the value of the variable is not set yet
     * @param description  The description of the variable
     */
    public void register(final String category, final String name, final String defaultValue, final String description) {
        this.register(category, name, defaultValue, description, false);
    }

    /**
     * Returns the string value of simple variable
     * It returns null in case of there is no provided category found or no variable with provided name found
     *
     * @param category The category name (e.g. "Layout")
     * @param keyValue The name of a variable
     * @return The value of simple variable
     */
    public String get(final String category, final String keyValue) {
        Object o = null;
        Map<String, Object> map;
        try {
            map = this.content.get(category, keyValue);
            if ((map == null) || (map.isEmpty())) {
                return null;
            }
            o = map.get(VALUE);
            if (o == null) {
                // no value defined - using default value
                o = map.get(DEFAULT);
                if (o == null) {
                    throw new IOException("Invalid structure of settings file.");
                }
            }

            String result = o.toString();

            o = map.get(ENCRYPTED);
            if ((o != null) && ("1".equals(o.toString()))) {
                result = ContentHandlerImpl.decrypt(result);
            }
            return result;

        } catch (IOException e) {
            LOGGER.error("Cannot get simple settings variable.", e);
            return null;
        }
    }

    /**
     * Returns the boolean value of simple variable
     * It returns true if variable found and equals "true", otherwise false
     *
     * @param category The category name (e.g. "Layout")
     * @param keyValue The name of a variable
     * @return The value of simple variable
     */
    public boolean getBoolean(final String category, final String keyValue) {
        return "true".equals(get(category, keyValue));
    }

    /**
     * Deletes the simple variable
     *
     * @param category The category name (e.g. "Layout")
     * @param name     The name of variable to be deleted
     */
    public void delete(final String category, final String name) {
        this.content.delete(category, KEYNAME, name);
    }
}
