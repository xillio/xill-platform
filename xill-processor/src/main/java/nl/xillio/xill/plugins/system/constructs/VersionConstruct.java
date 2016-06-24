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
package nl.xillio.xill.plugins.system.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.system.services.version.VersionProvider;
import org.slf4j.Logger;

import java.util.Arrays;

/**
 * Returns a string containing the current version
 */
public class VersionConstruct extends Construct {

    @Inject
    private VersionProvider versionProvider;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                v -> process(v, context.getRootLogger(), versionProvider),
                new Argument("requiredVersion", NULL, ATOMIC));
    }

    static MetaExpression process(final MetaExpression requiredVersion, final Logger log, final VersionProvider versionProvider) {
        String version = versionProvider.getVersion();

        if (version.equals(VersionProvider.DEVELOP)) {
            log.warn("Running in develop mode, all versions are accepted.");
        } else if (requiredVersion != NULL) {
            try {
                int[] versionParts = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
                int[] requiredVersionParts = Arrays.stream(requiredVersion.getStringValue().split("\\.")).mapToInt(Integer::parseInt).toArray();

                for (int i = 0; i < requiredVersionParts.length && i < versionParts.length; i++) {
                    if (versionParts[i] > requiredVersionParts[i]) {
                        // This is a newer version
                        break;
                    }

                    if (versionParts[i] < requiredVersionParts[i]) {
                        // This is an older version
                        throw new RobotRuntimeException("Version " + requiredVersion.getStringValue() + " is not supported in " + version);
                    }
                }

            } catch (NumberFormatException e) {
                log.error("Failed to parse version number", e);
            }

        }

        return fromValue(version);
    }
}
