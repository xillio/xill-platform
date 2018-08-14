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
package nl.xillio.xill.plugins.xml.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.plugins.xml.services.XsdService;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the {@link XsdCheckConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class XsdCheckConstructTest extends TestUtils {

    /**
     * Test the process method under normal circumstances
     */
    @Test
    public void testProcess() {
        // Mock
        Logger logger = mock(Logger.class);
        XsdService xsdService = mock(XsdService.class);

        MetaExpression xsdFilenameVar = mock(MetaExpression.class);
        when(xsdFilenameVar.getStringValue()).thenReturn(".");
        MetaExpression xmlFilenameVar = mock(MetaExpression.class);
        when(xmlFilenameVar.getStringValue()).thenReturn(".");

        File file = mock(File.class);
        RobotID id = RobotID.dummyRobot();

        ConstructContext context = mock(ConstructContext.class);
        when(context.getRobotID()).thenReturn(id);

        when(xsdService.xsdCheck(any(), any(), any())).thenReturn(true);

        // Run
        MetaExpression result = XsdCheckConstruct.process(context, xsdFilenameVar, xmlFilenameVar, fromValue(false), xsdService, logger);

        // Verify
        verify(xsdService).xsdCheck(any(), any(), any());

        // Assert
        assertTrue(result.getBooleanValue());
    }
}
