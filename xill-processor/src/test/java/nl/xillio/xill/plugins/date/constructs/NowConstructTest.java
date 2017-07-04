/**
 * Copyright (C) 2014 Xillio (support@xillio.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.xillio.xill.plugins.date.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.plugins.date.services.DateServiceImpl;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;

import static org.testng.Assert.assertTrue;

/**
 * Tests the {@link NowConstruct}
 *
 * @author Geert Konijnendijk
 */
public class NowConstructTest extends TestUtils {

    /**
     * Test the process method under the default circumstances
     */
    @Test
    public void testProcess() {
        NowConstruct nowConstruct = new NowConstruct();
        nowConstruct.setDateService(new DateServiceImpl());

        // Run
        MetaExpression date = process(nowConstruct);

        // Assert
        ZonedDateTime dateTime = date.getMeta(Date.class).getZoned();

        assertTrue(dateTime.isAfter(ZonedDateTime.now().minusMinutes(1)));
        assertTrue(dateTime.isBefore(ZonedDateTime.now().plusMinutes(1)));
    }
}
