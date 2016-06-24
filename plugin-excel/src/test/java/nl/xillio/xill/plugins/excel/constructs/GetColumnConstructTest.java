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
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillRow;
import nl.xillio.xill.plugins.excel.datastructures.XillSheet;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for the getColumn construct.
 *
 * @author Xavier Pardonnet
 */
public class GetColumnConstructTest extends TestUtils {

    /**
     * Creates a mock for a XillSheet that will return the same for each cell in the sheet.
     *
     * @return a MetaExpression containing the mocked {@link XillSheet}
     */
    private MetaExpression createSheetInput() {
        XillSheet sheet = mock(XillSheet.class);
        XillRow row = mock(XillRow.class);
        LinkedHashMap<String, MetaExpression> sheetObject = new LinkedHashMap<>();
        MetaExpression sheetInput = fromValue(sheetObject);
        sheetInput.storeMeta(sheet);
        when(sheet.getRow(any(Integer.class))).thenReturn(row);
        return sheetInput;
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testInvalidColumn() throws Exception {
        MetaExpression sheet = createSheetInput();
        MetaExpression colNum = fromValue(-1);
        GetColumnConstruct getColumn = new GetColumnConstruct();
        process(getColumn, sheet, colNum);
    }

    @Test
    public void testHasIteratorNumeric() throws Exception {
        MetaExpression sheet = createSheetInput();
        MetaExpression colNum = fromValue(1);
        GetColumnConstruct getColumn = new GetColumnConstruct();
        MetaExpression result = process(getColumn, sheet, colNum);
        assertNotNull(result.getMeta(MetaExpressionIterator.class));
    }

    @Test
    public void testHasIteratorAlpha() throws Exception {
        MetaExpression sheet = createSheetInput();
        MetaExpression colNum = fromValue("A");
        GetColumnConstruct getColumn = new GetColumnConstruct();
        MetaExpression result = process(getColumn, sheet, colNum);
        assertNotNull(result.getMeta(MetaExpressionIterator.class));
    }


}