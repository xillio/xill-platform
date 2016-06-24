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
package nl.xillio.xill.plugins.math.constructs;

import me.biesaart.utils.Log;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import org.slf4j.Logger;

/**
 * This construct will return the meaning of life as proven by Deep Thought
 */
public class PowerConstuct extends Construct {

    private static final Logger LOGGER = Log.get();

    @Override
    public String getName() {
        return "meaningOfLife";
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(() -> {
            context.getRootLogger().info("Calculating the answer to the ultimate question of everything.... Please hold on 7.5 million years...");
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            return fromValue(42);
        });
    }

    @Override
    public boolean hideDocumentation() {
        return true;
    }
}
