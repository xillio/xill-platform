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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface covers all needed methods to interact with the data holder for the Settings purposes.
 * That data holder can be database, file, etc. It depends just on the implementation.
 *
 * @author Zbynek Hochmann
 */
public interface ContentHandler {
    /**
     * Initialize the target content provider
     * E.g. opens existing file or creates the new file and creates the basic structure or connects to DB, creates the schema if not done yet, etc.
     */
    void init() throws IOException;

    /**
     * Returns all item values for item specified by category and key value.
     * This is used for getting one concrete item having key specified.
     *
     * @param category The name of the category (e.g. "Layout")
     * @param keyValue The value of the item's key (e.g. "LeftPanelWidth")
     * @return The list of item values where key is the name of value
     * @throws IOException              if file structure is invalid
     * @throws IllegalArgumentException if category contains illegal XPath characters
     */
    Map<String, Object> get(final String category, final String keyValue) throws IOException;

    /**
     * Returns list of items. The item structure is the same as described in get().
     * This is usually applied to category having items without keys but it can be used for even items having key (but the information what is key for particular item won't be there).
     *
     * @param category The name of the category (e.g. "Layout")
     * @return The list of items
     * @throws IOException              if file structure is invalid
     * @throws IllegalArgumentException if category contains illegal XPath characters
     */
    List<Map<String, Object>> getAll(final String category) throws IOException;

    /**
     * Sets one item - it means it will add the item if does not exist or update the values if already exists.
     * The values that already exists and not in itemContent will be left unchanged -
     * - the only way to delete them is to delete the item first and then call this set() (e.g. add new one).
     *
     * @param category    The name of the category (e.g. "Layout")
     * @param itemContent The content of one item - e.g. list of item values
     * @param keyName     Specifies what item value is the key for the item (this can be null in case of adding keyless item - the item has no key then - it's used for simple list usage)
     * @param keyValue    Specifies what value has key - it's used just for looking for existing item (it can be null if keyName is null)
     * @return true if the new item was created, false if the item already exists
     * @throws IllegalArgumentException if category, key or value contain illegal XPath characters
     */
    boolean set(final String category, final Map<String, Object> itemContent, final String keyName, final String keyValue);

    /**
     * Deletes existing item
     * It will try to find the existing item according to category and item's key
     *
     * @param category The name of the category (e.g. "Layout")
     * @param keyName  Specifies what item value is the key for the item
     * @param keyValue Specifies the value of that key
     * @return true if item was found and deleted, false if not found (i.e. not deleted)
     * @throws IllegalArgumentException if some problem occurs
     * @throws IllegalArgumentException if category, key or value contain illegal XPath characters
     */
    boolean delete(final String category, final String keyName, final String keyValue);

    /**
     * Checks the existence of the item
     *
     * @param category The name of the category (e.g. "Layout")
     * @param keyName  Specifies what item value is the key for the item
     * @param keyValue Specifies the value of that key
     * @return true if item was found (i.e. exists), false if item was not found (i.e. does not exist)
     * @throws IllegalArgumentException if category, key or value contain illegal XPath characters
     */
    boolean exist(final String category, final String keyName, final String keyValue);

    /**
     * Allow to use mechanism that stores the settings changes but does not automatically save them after each change -
     * - in such case the commit() must be called to save all changes done from last commit() if the manual commit is activated
     * This feature is not mandatory. If not supported then every change done is saved to target medium immediately.
     * DEFAULT is that manual commit = off
     *
     * @param manual true means that commit() must be called to save all changes done from last commit() to target, otherwise the changes are save to target immediately
     * @return true if the manual commit is supported and set on/off; false if this feature is not supported
     */
    boolean setManualCommit(boolean manual);

    /**
     * It save all changes done from last commit() if the manual commit is activated
     *
     * @return true if the commit has been done, false if the manual commit feature is not supported
     */
    boolean commit();
}
