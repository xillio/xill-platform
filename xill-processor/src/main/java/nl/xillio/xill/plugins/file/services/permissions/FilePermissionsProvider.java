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

import com.google.inject.ImplementedBy;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This interface represents an object that can produce a summary of file permissions regardless of operating system.
 *
 * @author Thomas Biesaart
 */
@ImplementedBy(DelegatePermissionsProviderImpl.class)
public interface FilePermissionsProvider {

    /**
     * read the declared permissions for a specific file.
     *
     * @param file the file
     * @return the permissions or null if no permissions could be extracted
     * @throws IOException  Is thrown if a failure of the file permissions occurs.
     */
    FilePermissions readPermissions(Path file) throws IOException;
}
