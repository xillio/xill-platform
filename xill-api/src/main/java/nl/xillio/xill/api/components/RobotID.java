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
package nl.xillio.xill.api.components;

import me.biesaart.utils.Log;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

/**
 * A unique identifier for robots.
 */
public class RobotID implements Serializable {
    private static final Logger LOGGER = Log.get();

    private static Map<URL, RobotID> ids = new Hashtable<>();
    private final URL url;

    private RobotID(final URL url) {
        this.url = url;
    }

    public URL getURL() {
        return url;
    }

    public String getName() {
        return url.toString().substring(url.toString().lastIndexOf("/") + 1);
    }

    @Override
    public String toString() {
        return "RobotID{" +
                "url=" + url +
                '}';
    }

    /**
     * Gets/creates a robotID that is singular for every path.
     *
     * @param url the robot file
     * @return a unique robot id for this path
     */
    public static Optional<RobotID> getInstance(final URL url) {
        if (url == null) {
            return Optional.empty();
        }

        return Optional.of(ids.computeIfAbsent(url, k -> new RobotID(url)));
    }

    /**
     * Used in tests to create a dummy ID.
     *
     * @return a dummy ID for testing.
     */
    public static RobotID dummyRobot() {
        String basicURL = "file:///";
        try {
            return new RobotID(new URL(basicURL));
        } catch (MalformedURLException e) {
            LOGGER.error(basicURL + " is not a valid URL", e);
        }
        return null;
    }
}
