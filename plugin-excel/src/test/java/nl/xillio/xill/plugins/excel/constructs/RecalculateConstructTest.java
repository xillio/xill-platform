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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.excel.datastructures.XillWorkbook;
import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the recalculate construct.
 *
 * @author Paul van der Zandt
 */
public class RecalculateConstructTest extends TestUtils {

    /**
     * Tests if the correct exception is thrown when there was no workbook in the input MetaExpression
     */
    @Test(expectedExceptions = RobotRuntimeException.class,
            expectedExceptionsMessageRegExp = "Expected parameter 'workbook' to be a result of loadWorkbook or createWorkbook")
    public void testProcessNoValidWorkbook() throws Exception {
        RecalculateConstruct construct = new RecalculateConstruct();
        process(construct, NULL);
    }

    /**
     * Tests normal processing of the construct
     */
    @Test
    public void testProcessNormal() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression metaWorkbook = fromValue("workbook");
        metaWorkbook.storeMeta(workbook);

        doNothing().when(workbook).recalculate();

        RecalculateConstruct construct = new RecalculateConstruct();
        assertEquals(process(construct, metaWorkbook), fromValue(true));
    }

    /**
     * Tests recalculating sheet with unimplemented function.
     */
    @Test(expectedExceptions = InvalidUserInputException.class,
            expectedExceptionsMessageRegExp = "Workbook contains unknown function 'TEST' in cell Sheet1!A1\\..*TEST.*")
    public void testProcessWithUnimplementedFunction() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression metaWorkbook = fromValue("workbook");
        metaWorkbook.storeMeta(workbook);

        NotImplementedException notImplementedException = new NotImplementedException("Error evaluating cell Sheet1!A1", new NotImplementedFunctionException("TEST"));
        doThrow(notImplementedException).when(workbook).recalculate();

        RecalculateConstruct construct = new RecalculateConstruct();
        process(construct, metaWorkbook);

    }

   /**
     * Tests recalculating sheet with some unimplemented exception..
     */
    @Test(expectedExceptions = OperationFailedException.class,
            expectedExceptionsMessageRegExp = "Could not recalculate workbook\\..*Error evaluating cell Sheet1!A1")
    public void testProcessWithUnknownImplementationFunction() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression metaWorkbook = fromValue("workbook");
        metaWorkbook.storeMeta(workbook);

        NotImplementedException notImplementedException = new NotImplementedException("Error evaluating cell Sheet1!A1", new NotImplementedException("NOT SHOWN"));
        doThrow(notImplementedException).when(workbook).recalculate();

        RecalculateConstruct construct = new RecalculateConstruct();
        process(construct, metaWorkbook);

    }

   /**
     * Tests recalculating sheet with unknown exception.
     */
    @Test(expectedExceptions = OperationFailedException.class,
            expectedExceptionsMessageRegExp = "Could not recalculate workbook\\..*Error recalculating")
    public void testProcessWithUnknownException() throws Exception {
        XillWorkbook workbook = mock(XillWorkbook.class);
        MetaExpression metaWorkbook = fromValue("workbook");
        metaWorkbook.storeMeta(workbook);

        RuntimeException runtimeException = new RuntimeException("Error recalculating");
        doThrow(runtimeException).when(workbook).recalculate();

        RecalculateConstruct construct = new RecalculateConstruct();
        process(construct, metaWorkbook);

    }

}
