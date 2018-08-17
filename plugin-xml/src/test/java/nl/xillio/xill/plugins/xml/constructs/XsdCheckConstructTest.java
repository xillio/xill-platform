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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the {@link XsdCheckConstructTest}
 *
 * @author Zbynek Hochmann
 */
public class XsdCheckConstructTest extends TestUtils {

    private Logger Logger;
    private XsdService xsdService;
    private MetaExpression xsdFilenameVar;
    private MetaExpression xmlFilenameVar;
    private RobotID robotId;
    private ConstructContext context;

    @BeforeMethod
    public void setUp() {
        Logger = mock(Logger.class);
        xsdService = mock(XsdService.class);
        robotId = RobotID.dummyRobot();

        xsdFilenameVar = mock(MetaExpression.class);
        when(xsdFilenameVar.getStringValue()).thenReturn(".");

        xmlFilenameVar = mock(MetaExpression.class);
        when(xmlFilenameVar.getStringValue()).thenReturn(".");

        context = mock(ConstructContext.class);
        when(context.getRobotID()).thenReturn(robotId);
    }

    /**
     * Test the process method for a straight true/false answer.
     */
    @Test
    public void testProcessBoolean() {
        when(xsdService.xsdCheck(any(), any(), any())).thenReturn(true);

        // Run
        MetaExpression result = XsdCheckConstruct.process(context, xsdFilenameVar, xmlFilenameVar, fromValue(false), xsdService, Logger);

        // Verify
        verify(xsdService).xsdCheck(any(), any(), any());

        // Assert
        assertTrue(result.getBooleanValue());
    }

    /**
     * Test the process method for a list of issues.
     */
    @Test
    public void testProcessList() {
        String issue1 = "issue1";
        String issue2 = "issue2";
        List<String> expectResult = new ArrayList();
        expectResult.add(issue1);
        expectResult.add(issue2);

        when(xsdService.xsdCheckGetIssueList(any(), any(), any())).thenReturn(expectResult);

        // Run
        MetaExpression result = XsdCheckConstruct.process(context, xsdFilenameVar, xmlFilenameVar, fromValue(true), xsdService, Logger);
        List<String> resultList = result.<List<MetaExpression>>getValue().stream()
                .map(MetaExpression::getStringValue)
                .collect(Collectors.toList());

        // Assert
        assertEqualsNoOrder(resultList.toArray(), expectResult.toArray());
    }
}
