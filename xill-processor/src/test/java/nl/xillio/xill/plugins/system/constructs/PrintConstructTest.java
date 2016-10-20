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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.services.json.JacksonParser;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test the {@link PrintConstruct}
 */
public class PrintConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcessError() {
        // Mock context
        String message = "This is the message";
        String level = "error";
        MetaExpression textVar = mockExpression(ATOMIC);
        when(textVar.getStringValue()).thenReturn(message);
        MetaExpression logLevel = mockExpression(ATOMIC);
        when(logLevel.getStringValue()).thenReturn(level);
        Logger robotLog = mock(Logger.class);

        // Run method
        PrintConstruct.process(textVar, logLevel, robotLog);

        // Verify calls
        verify(robotLog).error(eq(message));
    }

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcessInfo() {
        // Mock context
        String message = "This is the message";
        String level = "";
        MetaExpression textVar = mockExpression(ATOMIC);
        when(textVar.getStringValue()).thenReturn(message);
        MetaExpression logLevel = mockExpression(ATOMIC);
        when(logLevel.getStringValue()).thenReturn(level);
        Logger robotLog = mock(Logger.class);

        // Run method
        PrintConstruct.process(textVar, logLevel, robotLog);

        // Verify calls
        verify(robotLog).info(eq(message));
    }

    @Test
    public void testProcessPrettyDebug() {
        // Mock context
        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
        map.put("test", ExpressionBuilderHelper.fromValue("value"));
        MetaExpression textVar = ExpressionBuilderHelper.fromValue(map);
        String level = "debug";
        MetaExpression logLevel = mockExpression(ATOMIC);
        when(logLevel.getStringValue()).thenReturn(level);
        Logger robotLog = mock(Logger.class);

        // Run method
        PrintConstruct construct = new PrintConstruct(new JacksonParser(true));
        construct.process(textVar, logLevel, robotLog);

        // Verify calls
        String message = String.format("{%1$s  \"test\" : \"value\"%1$s}", System.getProperty("line.separator"));
        verify(robotLog).debug(eq(message));
    }
}
