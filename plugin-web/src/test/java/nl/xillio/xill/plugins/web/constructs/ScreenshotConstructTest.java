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
package nl.xillio.xill.plugins.web.constructs;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.FileService;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ScreenshotConstruct}.
 */
public class ScreenshotConstructTest extends ExpressionBuilderHelper {

    /**
     * test the process with normal usage.
     *
     * @throws IOException
     */
    @Test
    public void testProcessNormalUsage() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mock(MetaExpression.class);
        when(name.getStringValue()).thenReturn(nameValue);

        // The files
        Path srcFile = mock(Path.class);
        Path desFile = mock(Path.class);

        // The options
        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        // The process
        when(webService.getScreenshotAsFilePath(pageVariable, 0, 0)).thenReturn(srcFile);
        when(TestUtils.CONSTRUCT_FILE_RESOLVER.buildPath(null, name)).thenReturn(desFile);

        // run
        MetaExpression output = ScreenshotConstruct.process(page, name, options, fileService, webService, null);

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // We make one screenshot and store it once
        verify(webService, times(1)).getScreenshotAsFilePath(pageVariable, 0, 0);
        verify(fileService, times(1)).copyFile(srcFile, desFile);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process with null page given.
     */
    @Test
    public void testNullInput() {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);
        MetaExpression input = mock(MetaExpression.class);
        MetaExpression fileName = mock(MetaExpression.class);
        when(input.isNull()).thenReturn(true);
        MetaExpression options = mock(MetaExpression.class);

        // run
        MetaExpression output = ScreenshotConstruct.process(input, fileName, options, fileService, webService, null);

        // assert
        Assert.assertEquals(output, NULL);
    }

    /**
     * Test the process when the webService breaks
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessWhenWebServiceBreaks() {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mock(MetaExpression.class);
        when(name.getStringValue()).thenReturn(nameValue);

        // The options
        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        // The process
        when(webService.getScreenshotAsFilePath(pageVariable, 0, 0)).thenThrow(new RobotRuntimeException(""));

        // run
        ScreenshotConstruct.process(page, name, options, fileService, webService, null);
    }

    /**
     * Test the process when the fileService breaks
     *
     * @throws IOException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Could not copy to: Tony.*")
    public void testProcessWhenFileServiceBreaks() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mock(MetaExpression.class);
        when(name.getStringValue()).thenReturn(nameValue);

        // The files
        Path srcFile = mock(Path.class);
        Path desFile = mock(Path.class);

        // The options
        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        // The process
        when(webService.getScreenshotAsFilePath(pageVariable, 0, 0)).thenReturn(srcFile);
        when(TestUtils.CONSTRUCT_FILE_RESOLVER.buildPath(null, name)).thenReturn(desFile);
        doThrow(new IOException()).when(fileService).copyFile(srcFile, desFile);

        // run
        MetaExpression output = ScreenshotConstruct.process(page, name, options, fileService, webService, null);

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // We make one screenshot and store it once
        verify(webService, times(1)).getScreenshotAsFilePath(pageVariable, 0, 0);
        verify(fileService, times(1)).copyFile(srcFile, desFile);

        // assert
        Assert.assertEquals(output, NULL);

    }

    /**
     * Test the process when no page is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoPageGiven() {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);

        // The page
        MetaExpression page = mock(MetaExpression.class);
        when(page.getMeta(PageVariable.class)).thenReturn(null);

        // The options
        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mock(MetaExpression.class);
        when(name.getStringValue()).thenReturn(nameValue);

        // run
        ScreenshotConstruct.process(page, name, options, fileService, webService, null);
    }

    /**
     * Test the process when no filename is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNameGiven() {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);

        // The name
        String nameValue = "";
        MetaExpression name = mock(MetaExpression.class);
        when(name.getStringValue()).thenReturn(nameValue);

        // The options
        MetaExpression options = mock(MetaExpression.class);
        when(options.isNull()).thenReturn(true);

        // The page
        MetaExpression page = mock(MetaExpression.class);

        // run
        ScreenshotConstruct.process(page, name, options, fileService, webService, null);
    }

}
