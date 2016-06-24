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
package nl.xillio.xill.plugins.system.services.wait;

import me.biesaart.utils.Log;
import nl.xillio.xill.services.XillService;
import org.slf4j.Logger;

/**
 * This service waits when called
 */
public class WaitService implements XillService {

    private static final Logger LOGGER = Log.get();

    /**
     * Wait for a time
     *
     * @param delay in milliseconds
     */
    public void wait(final int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            LOGGER.error("Wait interrupted: " + e.getMessage(), e);
        }
    }
}
