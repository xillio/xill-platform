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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * This provider can read posix permissions.
 *
 * @author Thomas Biesaart
 */
public class PosixFilePermissionsProvider implements FilePermissionsProvider {
    @Override
    public FilePermissions readPermissions(Path file) throws IOException {
        PosixFileAttributeView posixView = Files.getFileAttributeView(file, PosixFileAttributeView.class);

        if (posixView == null) {
            // We have no results
            return null;
        }

        PosixFileAttributes attributes = posixView.readAttributes();
        Set<PosixFilePermission> permissionsSet = attributes.permissions();

        FilePermissions permissions = new FilePermissions(file);

        // Group
        boolean readGroup = permissionsSet.contains(GROUP_READ);
        boolean writeGroup = permissionsSet.contains(GROUP_WRITE);
        boolean executeGroup = permissionsSet.contains(GROUP_EXECUTE);
        permissions.setGroup(attributes.group().getName(), readGroup, writeGroup, executeGroup);

        // User
        boolean readUser = permissionsSet.contains(OWNER_READ);
        boolean writeUser = permissionsSet.contains(OWNER_WRITE);
        boolean executeUser = permissionsSet.contains(OWNER_EXECUTE);
        permissions.setUser(attributes.owner().getName(), readUser, writeUser, executeUser);

        return permissions;
    }
}
