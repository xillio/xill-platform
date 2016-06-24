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
package nl.xillio.xill.plugins.file.services.permissions;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents a summary of the declared permissions on a file.
 *
 * @author Thomas Biesaart
 */
public class FilePermissions {
    private final Path file;
    private final Map<String, FileOperations> users = new HashMap<>();
    private final Map<String, FileOperations> groups = new HashMap<>();

    public FilePermissions(Path file) {
        this.file = file;
    }

    public void setUser(String user, boolean readable, boolean writable, boolean executable) {
        setOn(users, user, new FileOperations(readable, writable, executable));
    }

    public void setGroup(String group, boolean readable, boolean writable, boolean executable) {
        setOn(groups, group, new FileOperations(readable, writable, executable));
    }

    /**
     * This method will set or combine file permissions on a map with a string index.
     *
     * @param map        the map
     * @param key        the key
     * @param operations the operations to add.
     */
    private void setOn(Map<String, FileOperations> map, String key, FileOperations operations) {
        FileOperations current = map.get(key);
        if (current == null) {
            map.put(key, operations);
        } else {
            FileOperations combined = new FileOperations(
                    current.isReadable() || operations.isReadable(),
                    current.isWritable() || operations.isWritable(),
                    current.isExecutable() || operations.isExecutable()
            );

            map.put(key, combined);
        }
    }

    public Path getFile() {
        return file;
    }

    public Map<String, Map<String, Map<String, Boolean>>> toMap() {
        Map<String, Map<String, Map<String, Boolean>>> result = new HashMap<>();
        result.put("users", toMap(this.users));
        result.put("groups", toMap(this.groups));

        return result;
    }

    private Map<String, Map<String, Boolean>> toMap(Map<String, FileOperations> input) {
        return input.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().toMap()
                ));
    }

}
