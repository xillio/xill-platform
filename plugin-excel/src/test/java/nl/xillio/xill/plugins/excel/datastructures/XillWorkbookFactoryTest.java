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
package nl.xillio.xill.plugins.excel.datastructures;

import nl.xillio.xill.api.errors.NotImplementedException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the XillWorkbok Factory
 *
 * @author Daan Knoope
 *         <p>
 *         NB: not all methods are tested, since this is a factory
 */
public class XillWorkbookFactoryTest {

    /**
     * Tests if loadWorkbook finds the extension of the paths and handles them correctly,
     * sending the xls extension to makeLegacyWorkbook,
     * the xlsx extension to makeModernWorkbook and
     * throwing a NotImplementedException otherwise.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = NotImplementedException.class,
            expectedExceptionsMessageRegExp = "This extension is not supported as Excel workbook\\.")
    public void testLoadWorkbook() throws Exception {
        File file = mock(File.class);
        when(file.getName()).thenReturn("file.xls");
        XillWorkbookFactory factory = spy(new XillWorkbookFactory());
        FileInputStream stream = mock(FileInputStream.class);
        doReturn(stream).when(factory).getStream(file);
        Workbook workbook = mock(Workbook.class);
        doReturn(workbook).when(factory).makeLegacyWorkbook(stream);
        factory.loadWorkbook(file);
        verify(factory, times(1)).makeLegacyWorkbook(stream);

        when(file.getName()).thenReturn("file.xlsx");
        workbook = mock(XSSFWorkbook.class);
        doReturn(workbook).when(factory).makeModernWorkbook(stream);
        factory.loadWorkbook(file);
        verify(factory, times(1)).makeModernWorkbook(stream);

        when(file.getName()).thenReturn("file.xlz");
        factory.loadWorkbook(file);
    }

}
