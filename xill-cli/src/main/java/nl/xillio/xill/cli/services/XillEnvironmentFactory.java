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
package nl.xillio.xill.cli.services;

import nl.xillio.xill.api.XillEnvironment;
import nl.xillio.xill.api.XillLoader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This class is responsible for building the Xill environment.
 *
 * @author Thomas Biesaart
 */
public class XillEnvironmentFactory {


    @SuppressWarnings("squid:S2095")
    // Don't close the XillEnvironment as we are returning it
    public XillEnvironment buildFor(Path coreFolder, Path... pluginFolders) throws IOException {
        if (coreFolder == null) {
            throw new IllegalArgumentException("No coreFolder provided");
        }

        XillEnvironment environment = XillLoader.getEnv(coreFolder);

        environment.addFolder(coreFolder);
        for (Path path : pluginFolders) {
            environment.addFolder(path);
        }

        return environment;
    }
}
