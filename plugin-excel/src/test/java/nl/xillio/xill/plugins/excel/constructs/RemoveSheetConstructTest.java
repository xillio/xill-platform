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

import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import nl.xillio.xill.plugins.excel.services.ExcelService;
import org.mockito.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the RemoveSheet construct
 *
 * @author Daan Knoope
 */
public class RemoveSheetConstructTest {

    /**
     * Creates a mocked XillWorkbook.
     *
     * @return a mocked {@link XillWorkbook}
     */
    private MetaExpression createWorkbook() {
        MetaExpression workbookInput = fromValue("Workbook");
        XillWorkbook workbook = mock(XillWorkbook.class);
        workbookInput.storeMeta(workbook);
        return workbookInput;
    }

    /**
     * Checks if the correct RobotRuntimeException is thrown when an {@link ExpressionDataType#OBJECT}
     * was provided instead of the required {@link ExpressionDataType#LIST} or {@link ExpressionDataType#ATOMIC}.
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "No valid \\(list of\\) sheetnames provided")
    public void testProcessThrowsExceptionAtObject() throws Exception {
        MetaExpression inputWorkbook = createWorkbook();
        MetaExpression sheetName = mock(MetaExpression.class);
        when(sheetName.getType()).thenReturn(ExpressionDataType.OBJECT);
        RemoveSheetConstruct.process(mock(ExcelService.class), inputWorkbook, sheetName);
    }

    /**
     * Checks if the correct method (single or multiple) is called from the constructor
     * depending on the ExpressionDataType.ATOMIC.
     */
    @Test
    public void testProcessSucceedsWithSingle() throws Exception {
        ExcelService service = mock(ExcelService.class);
        MetaExpression inputWorkbook = createWorkbook();
        MetaExpression sheetName = mock(MetaExpression.class);
        when(sheetName.getType()).thenReturn(ExpressionDataType.ATOMIC);

        //Check
        assertTrue(RemoveSheetConstruct.process(service, inputWorkbook, sheetName).getBooleanValue());
    }

    /**
     * Checks if the correct method (single or multiple) is called from the constructor
     * depending on the provided list
     *
     * @throws Exception
     */
    @Test
    public void testProcessSucceedsWithMultiple() throws Exception {
        ExcelService service = mock(ExcelService.class);
        MetaExpression inputWorkbook = createWorkbook();
        List<MetaExpression> inputSheets = Arrays.asList(fromValue("sheet1"), fromValue("sheet2"));

        //Check
        assertTrue(RemoveSheetConstruct.process(service, inputWorkbook, fromValue(inputSheets)).getBooleanValue());
    }

    /**
     * Checks if the processSingle method returns {@code true} back to the construct
     * after having called the removeSheet method in the service (which will take care of the rest).
     */
    @Test
    public void testProcessSingleRemovesSheet() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression sheetName = fromValue("sheet");
        boolean result = RemoveSheetConstruct.processSingle(service, workbook, sheetName).getBooleanValue();

        verify(service, times(1)).removeSheet(workbook, "sheet");
        assertTrue(result);
    }

    /**
     * Tests if a RobotRuntimeException is thrown when the sheet that should be removed does not exist in
     * the workbook
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessSingleThrowsException() throws Exception {
        ExcelService service = mock(ExcelService.class);
        doThrow(new IllegalArgumentException()).when(service).removeSheet(any(XillWorkbook.class), anyString());
        RemoveSheetConstruct.processSingle(service, mock(XillWorkbook.class), fromValue("sheet"));
    }

    /**
     * Tests if a RobotRuntimeException is thrown when one or more of the sheets does not exist in the workbook
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessMultipleThrowsException() throws Exception {
        ExcelService service = mock(ExcelService.class);
        List<MetaExpression> inputSheets = Arrays.asList(fromValue("sheet1"), fromValue("sheet2"));
        doThrow(new IllegalArgumentException()).when(service).removeSheets(any(XillWorkbook.class), Matchers.anyListOf(String.class));
        RemoveSheetConstruct.processMultiple(service, mock(XillWorkbook.class), fromValue(inputSheets));
    }

    /**
     * Checks if the processMultiple method returns {@code true} back to th construct
     * after having called the removeSheets method in the service (which will take care of the rest)
     */
    @Test
    public void testProcessMultipleSucceeds() throws Exception {
        ExcelService service = mock(ExcelService.class);
        XillWorkbook workbook = mock(XillWorkbook.class);
        List<MetaExpression> inputSheets = Arrays.asList(fromValue("sheet1"), fromValue("sheet2"));
        assertTrue(RemoveSheetConstruct.processMultiple(service, workbook, fromValue(inputSheets)).getBooleanValue());

    }
}
