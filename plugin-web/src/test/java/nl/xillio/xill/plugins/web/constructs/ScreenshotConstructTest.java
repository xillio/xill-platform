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
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.FileService;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Test the {@link ScreenshotConstruct}.
 */
public class ScreenshotConstructTest extends TestUtils {

    /**
     * test the process with normal usage.
     *
     * @throws IOException
     */
    @Test
    public void testProcessNormalUsage() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);
        construct.setFileService(fileService);
        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // The files
        Path srcFile = mock(Path.class);
        // The options
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(true);

        // The process
        when(webService.getScreenshotAsFilePath(pageVariable, 0, 0)).thenReturn(srcFile);

        // run
        MetaExpression output = process(construct,page, name, options);

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // We make one screenshot and store it once
        verify(webService, times(1)).getScreenshotAsFilePath(pageVariable, 0, 0);
        verify(fileService, times(1)).copyFile(any(), any());

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
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);
        MetaExpression input = mockExpression(ATOMIC);
        MetaExpression fileName = mockExpression(ATOMIC);
        when(input.isNull()).thenReturn(true);
        MetaExpression options = mockExpression(OBJECT);

        // run
        MetaExpression output = process(construct,input, fileName, options);

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
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // The options
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(true);

        // The process
        when(webService.getScreenshotAsFilePath(pageVariable, 0, 0)).thenThrow(new RobotRuntimeException(""));

        // run
        process(construct,page, name, options);
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
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);
        construct.setFileService(fileService);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // The files
        Path srcFile = mock(Path.class);
        Path desFile = mock(Path.class);

        // The options
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(true);

        // The process
        when(webService.getScreenshotAsFilePath(pageVariable, 0, 0)).thenReturn(srcFile);
        when(TestUtils.CONSTRUCT_FILE_RESOLVER.buildPath(null, name)).thenReturn(desFile);
        doThrow(new IOException()).when(fileService).copyFile(any(),any());
        // run
        MetaExpression output = process(construct,page, name, options);

        // Wheter we parse the pageVariable only once
        verify(page, times(2)).getMeta(PageVariable.class);

        // We make one screenshot and store it once
        verify(webService, times(1)).getScreenshotAsFilePath(pageVariable, 0, 0);
        verify(fileService, times(1)).copyFile(any(), any());

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
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);

        // The page
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(null);

        // The options
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(true);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // run
        process(construct,page, name, options);
    }

    /**
     * Test the process when no filename is given.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessNoNameGiven() {
        // mock
        WebService webService = mock(WebService.class);
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);

        // The name
        String nameValue = "";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // The options
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(true);

        // The page
        MetaExpression page = mockExpression(ATOMIC);

        // run
        process(construct,page, name, options);
    }

    /**
     * test the process with invalid options.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid options.*")
    public void testProcessInvalidOptions() {
        // mock
        WebService webService = mock(WebService.class);
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // The options
        Map<String, MetaExpression> optionMap = new HashMap<>();
        optionMap.put("na", null);
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(false);
        when(options.getValue()).thenReturn(optionMap);

        // run
        process(construct,page, name, options);
    }

    /**
     * test the process with invalid resolution.
     */
    @Test(expectedExceptions = InvalidUserInputException.class, expectedExceptionsMessageRegExp = "Invalid resolution value.*")
    public void testProcessInvalidResolution() {
        // mock
        WebService webService = mock(WebService.class);
        ScreenshotConstruct construct = new ScreenshotConstruct();
        construct.setWebService(webService);
        FileService fileService = mock(FileService.class);

        // The page
        PageVariable pageVariable = mock(PageVariable.class);
        MetaExpression page = mockExpression(ATOMIC);
        when(page.getMeta(PageVariable.class)).thenReturn(pageVariable);

        // The name
        String nameValue = "Tony";
        MetaExpression name = mockExpression(ATOMIC);
        when(name.getStringValue()).thenReturn(nameValue);

        // The options
        Number resNum = new Integer(10); // Width and height lower than minimum allowed
        MetaExpression resValue = mockExpression(ATOMIC);
        when(resValue.getNumberValue()).thenReturn(resNum);
        List<MetaExpression> list = new LinkedList<>();
        list.add(resValue);
        list.add(resValue);
        MetaExpression value = mockExpression(LIST);
        when(value.getType()).thenReturn(LIST);
        when(value.getValue()).thenReturn(list);
        Map<String, MetaExpression> optionMap = new HashMap<>();
        optionMap.put("resolution", value);
        MetaExpression options = mockExpression(OBJECT);
        when(options.isNull()).thenReturn(false);
        when(options.getValue()).thenReturn(optionMap);

        // run
        process(construct,page, name, options);
    }
}
