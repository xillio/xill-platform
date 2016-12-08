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
package nl.xillio.xill.webservice.xill;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Properties for locating anf loading the Xill environment.
 *
 * @author Thomas Biesaart
 * @author Geert Konijnendijk
 */
@Component
@ConfigurationProperties(prefix = "runtime")
public class RuntimeProperties {
    private Path pluginDir = null;
    private Path licenseDir = null;

    public Path getLicenseDir() {
        return licenseDir;
    }

    public void setLicenseDir(Path licenseDir) {
        if (Files.isRegularFile(licenseDir)) {
            this.licenseDir = licenseDir.getParent();
        } else {
            this.licenseDir = licenseDir;
        }
    }

    public Path getPluginDir() {
        return pluginDir;
    }

    public void setPluginDir(Path pluginDir) {
        this.pluginDir = pluginDir;
    }
}
