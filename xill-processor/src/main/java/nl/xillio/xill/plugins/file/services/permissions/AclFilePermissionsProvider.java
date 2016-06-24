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
import java.nio.file.attribute.*;

/**
 * This provider can read acl permissions.
 *
 * @author Thomas Biesaart
 */
public class AclFilePermissionsProvider implements FilePermissionsProvider {
    @Override
    public FilePermissions readPermissions(Path file) throws IOException {
        AclFileAttributeView aclView = Files.getFileAttributeView(file, AclFileAttributeView.class);
        if (aclView == null) {
            // We have no results
            return null;
        }

        FilePermissions permissions = new FilePermissions(file);

        for (AclEntry entry : aclView.getAcl()) {
            UserPrincipal principal = entry.principal();
            boolean read = entry.permissions().contains(AclEntryPermission.READ_DATA);
            boolean write = entry.permissions().contains(AclEntryPermission.WRITE_DATA);
            boolean execute = entry.permissions().contains(AclEntryPermission.EXECUTE);

            if (principal instanceof GroupPrincipal) {
                permissions.setGroup(principal.getName(), read, write, execute);
            } else {
                permissions.setUser(principal.getName(), read, write, execute);
            }

        }

        return permissions;
    }
}
