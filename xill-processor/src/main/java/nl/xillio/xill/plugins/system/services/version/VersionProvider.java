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
package nl.xillio.xill.plugins.system.services.version;

import nl.xillio.xill.services.XillService;

/**
 * This class get's the running version
 */
public class VersionProvider implements XillService {

    /**
     * The string used when no implementation version is found.
     */
    public static final String DEVELOP = "Development";

    /**
     * Get the current version
     *
     * @return the current version
     */
    public String getVersion() {
        String version = getClass().getPackage().getSpecificationVersion();

        if (version == null) {
            version = DEVELOP;
        }

        return version;
    }

}
