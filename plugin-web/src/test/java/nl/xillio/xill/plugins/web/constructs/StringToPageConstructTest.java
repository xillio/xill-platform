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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.OptionsFactory;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.data.PhantomJSPool;
import nl.xillio.xill.plugins.web.services.web.FileService;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * test the {@link FromString}.
 */
public class StringToPageConstructTest {

    /**
     * Test the process under normal circumstances.
     *
     * @throws IOException
     */
    @Test
    public void testProcessNormalUsage() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);
        OptionsFactory optionsFactory = mock(OptionsFactory.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);

        // the content variable
        String contentValue = "This is the content";
        MetaExpression content = mock(MetaExpression.class);
        when(content.getStringValue()).thenReturn(contentValue);

        // the file variable
        File file = mock(File.class);

        // the process
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        PageVariable pageVariable = mock(PageVariable.class);
        when(fileService.createTempFile(anyString(), anyString())).thenReturn(file);
        when(fileService.getAbsolutePath(file)).thenReturn("file");
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);

        // run
        FromString.process(content, optionsFactory, fileService, webService, pool);

        // verify
        verify(fileService, times(1)).createTempFile(anyString(), anyString());
        verify(fileService, times(1)).getAbsolutePath(file);
        verify(webService, times(1)).getEntityFromPool(any(), any());
    }

    /**
     * Test the process when an IO exception is thrown
     *
     * @throws IOException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testProcessIOException() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);
        OptionsFactory optionsFactory = mock(OptionsFactory.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);

        // the content variable
        String contentValue = "This is the content";
        MetaExpression content = mock(MetaExpression.class);
        when(content.getStringValue()).thenReturn(contentValue);

        // the process
        when(fileService.createTempFile(anyString(), anyString())).thenThrow(new IOException());

        // run
        FromString.process(content, optionsFactory, fileService, webService, pool);
    }

    /**
     * Test the process when another exception is thrown.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "error")
    public void testProcessExceptionThrown() throws Exception {
        // mock
        WebService webService = mock(WebService.class);
        FileService fileService = mock(FileService.class);
        OptionsFactory optionsFactory = mock(OptionsFactory.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);

        // the content variable
        String contentValue = "This is the content";
        MetaExpression content = mock(MetaExpression.class);
        when(content.getStringValue()).thenReturn(contentValue);

        // the process
        when(fileService.createTempFile(anyString(), anyString())).thenThrow(new RobotRuntimeException("error"));

        // run
        FromString.process(content, optionsFactory, fileService, webService, pool);
    }
}
