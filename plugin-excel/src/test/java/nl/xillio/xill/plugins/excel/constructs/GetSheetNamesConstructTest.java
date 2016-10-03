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
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the GetSheetNames construct
 *
 * @author Daan Knoope
 */
public class GetSheetNamesConstructTest extends TestUtils {

    /**
     * Creates a XillWorkbook on which the test operations will be performed
     *
     * @param sheetNames a list of names which the sheets have
     * @return a mocked XillWorkbook
     */
    private MetaExpression createWorkbookInput(List<String> sheetNames) {
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression workbookInput = fromValue("workbook");
        workbookInput.storeMeta(workbook);
        when(workbook.getSheetNames()).thenReturn(sheetNames);
        return workbookInput;
    }

    /**
     * Checks if an empty list is returned when no sheets exist
     */
    @Test
    public void testProcessReturnsEmptyList() throws Exception {
        MetaExpression workbookInput = createWorkbookInput(new ArrayList<>());
        String result = GetSheetNamesConstruct.process(workbookInput).getStringValue();
        assertEquals(result, "[ ]");
    }

    /**
     * Checks if a correct list is returned when there are sheets in the workbook
     */
    @Test
    public void testProcessReturnsList() throws Exception {
        MetaExpression workbookInput = createWorkbookInput(Arrays.asList("Sheet1", "Sheet2"));
        String result = GetSheetNamesConstruct.process(workbookInput).getStringValue();
        assertEquals(result, String.format("[%1$s  \"Sheet1\",%1$s  \"Sheet2\"%1$s]", System.getProperty("line.separator")));
    }
}
