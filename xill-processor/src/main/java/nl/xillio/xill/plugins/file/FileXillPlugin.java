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
package nl.xillio.xill.plugins.file;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import nl.xillio.plugins.XillPlugin;
import nl.xillio.xill.plugins.file.services.files.SimpleTextFileReader;
import nl.xillio.xill.plugins.file.services.permissions.AclFilePermissionsProvider;
import nl.xillio.xill.plugins.file.services.permissions.FilePermissionsProvider;
import nl.xillio.xill.plugins.file.services.permissions.PosixFilePermissionsProvider;
import nl.xillio.xill.services.files.TextFileReader;

import java.util.Arrays;
import java.util.List;

/**
 * This package includes all example constructs.
 */
public class FileXillPlugin extends XillPlugin {

    @Provides
    @Singleton
    List<FilePermissionsProvider> filePermissionsProviders(AclFilePermissionsProvider acl, PosixFilePermissionsProvider posix) {
        return Arrays.asList(acl, posix);
    }

    @Override
    public void configure() {
        super.configure();
        bind(TextFileReader.class).toInstance(new SimpleTextFileReader());
    }
}
