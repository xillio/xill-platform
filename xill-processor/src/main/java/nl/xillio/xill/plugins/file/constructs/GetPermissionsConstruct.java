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
package nl.xillio.xill.plugins.file.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.file.services.permissions.FilePermissions;
import nl.xillio.xill.plugins.file.services.permissions.FilePermissionsProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * This construct will use the {@link FilePermissionsProvider} to get the declared permissions for a file.
 *
 * @author Thomas Biesaart
 */
public class GetPermissionsConstruct extends AbstractFilePropertyConstruct<FilePermissions> {
    private final FilePermissionsProvider permissionsProvider;

    @Inject
    public GetPermissionsConstruct(FilePermissionsProvider permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
    }

    @Override
    protected FilePermissions process(Path path) throws IOException {
        try {
            return permissionsProvider.readPermissions(path);
        } catch (IOException e) {
            throw new OperationFailedException("read permissions for " + path, e.getMessage(), e);
        }
    }

    @Override
    protected MetaExpression parse(FilePermissions input) {
        LinkedHashMap<String, Object> mapResult = new LinkedHashMap<>(input.toMap());
        return parseObject(mapResult);
    }
}
