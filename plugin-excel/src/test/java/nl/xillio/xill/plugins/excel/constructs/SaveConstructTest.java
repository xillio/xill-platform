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
package nl.xillio.xill.plugins.excel.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import nl.xillio.xill.plugins.excel.services.ExcelService;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the save construct
 *
 * @author Daan Knoope
 */
public class SaveConstructTest extends TestUtils {

    /**
     * Tests if the correct exception is thrown when there was no workbook in the input MetaExpression
     */
    @Test(expectedExceptions = RobotRuntimeException.class,
            expectedExceptionsMessageRegExp = "Expected parameter 'workbook' to be a result of loadWorkbook or createWorkbook")
    public void testProcessNoValidWorkbook() throws Exception {
        ExcelService service = mock(ExcelService.class);
        ConstructContext context = mock(ConstructContext.class);

        SaveConstruct.process(service, context, NULL, fromValue("path"));
    }

    /**
     * Tests the overwrite method of the save construct returns correctly
     */
    @Test
    public void testProcessOverrideByDefault() throws Exception {

        //Basic vars
        ExcelService service = mock(ExcelService.class);
        ConstructContext context = mock(ConstructContext.class);
        XillWorkbook workbook = mock(XillWorkbook.class);

        //Mock workbook
        MetaExpression workbookInput = fromValue("workbook");
        workbookInput.storeMeta(workbook);

        when(service.save(any(XillWorkbook.class))).thenReturn("overridden");
        assertEquals(SaveConstruct.process(service, context, workbookInput, NULL).getStringValue(), "overridden");
    }

    /**
     * Checks if construct returns correctly when a new save location (path)
     * is provided
     */
    @Test
    public void testProcessPath() throws Exception {

        //Basic vars
        ExcelService service = mock(ExcelService.class);
        ConstructContext context = mock(ConstructContext.class);
        MetaExpression inputPath = fromValue("path");

        //Mock RobotID
        RobotID id = mock(RobotID.class);
        when(id.getPath()).thenReturn(new File("."));
        when(context.getRobotID()).thenReturn(id);
        when(context.getRootRobot()).thenReturn(id);

        //mock workbook
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression workbookInput = fromValue("workbook");
        workbookInput.storeMeta(workbook);

        XillWorkbook returnbook = mock(XillWorkbook.class);
        when(returnbook.getLocation()).thenReturn("path");

        when(service.save(any(XillWorkbook.class), any(File.class))).thenReturn(returnbook);
        assertEquals(SaveConstruct.process(service, context, workbookInput, inputPath).getStringValue(), "Saved [path]");
    }

    /**
     * Checks if a RobotRuntimeException has been thrown when there was a write problem
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessOverwriteThrowsException() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        doThrow(new IOException()).when(service).save(any(XillWorkbook.class));

        SaveConstruct.processOverwrite(service, workbook);
    }

    /**
     * Checks if the overwrite method returns the right correctly after having written the file
     *
     * @throws Exception
     */
    @Test
    public void testProcessOverwrite() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(service.save(any(XillWorkbook.class))).thenReturn("Correctly Saved");
        assertEquals(SaveConstruct.processOverwrite(service, workbook), fromValue("Correctly Saved"));
    }

    /**
     * Checks a RobotRuntimeException is thrown when the file in the new path cannot be written to
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessToFolderThrowsException() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(service.save(any(XillWorkbook.class), any(File.class))).thenThrow(new IOException());
        SaveConstruct.processToFolder(service, workbook, mock(File.class));
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Cannot write to this file: read-only")
    public void testProcessOverwriteThrowsIllegalArgumentException() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        when(service.save(workbook)).thenThrow(new IllegalArgumentException("Cannot write to this file: read-only"));
        SaveConstruct.processOverwrite(service, workbook);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Cannot write to this file: read-only")
    public void testProcessToFolderThrowsIllegalArgumentException() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        File file = mock(File.class);
        when(service.save(workbook, file)).thenThrow(new IllegalArgumentException("Cannot write to this file: read-only"));
        SaveConstruct.processToFolder(service, workbook, file);
    }
}
