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
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * Test the {@link DownloadConstruct}.
 */
public class DownloadConstructTest extends TestUtils {

    /**
     * Test the process with normal usage
     *
     * @throws IOException
     */
    @Test
    public void testProcessNormalUsage() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        DownloadConstruct construct = new DownloadConstruct();
        construct.setWebService(webService);

        // The page
        PageVariable webContext = mock(PageVariable.class);
        MetaExpression webContextVar = mockExpression(ATOMIC);
        when(webContextVar.getMeta(PageVariable.class)).thenReturn(webContext);

        // The URL
        String url = "http://www.something.com/doc.pdf";
        MetaExpression urlVar = mockExpression(ATOMIC);
        when(urlVar.getStringValue()).thenReturn(url);

        // The target file
        String fileName = "c:/tmp/doc.pdf";
        MetaExpression targetFileVar = mockExpression(ATOMIC);
        when(targetFileVar.getStringValue()).thenReturn(fileName);
        Path targetFile = mock(Path.class);
        when(TestUtils.CONSTRUCT_FILE_RESOLVER.buildPath(null, targetFileVar)).thenReturn(targetFile);
        TestUtils.setFileResolverReturnValue(targetFile);

        Number timeoutNumber = 5000;
        MetaExpression timeoutVar = mockExpression(ATOMIC);
        when(timeoutVar.getNumberValue()).thenReturn(timeoutNumber);
        when(timeoutVar.getNumberValue().intValue()).thenReturn(timeoutNumber.intValue());

        // run
        MetaExpression output = process(construct, urlVar, targetFileVar, webContextVar, timeoutVar);

        // verify
        verify(webContextVar, times(2)).getMeta(PageVariable.class);
        verify(webService, times(1)).download(url, targetFile, webContext, timeoutNumber.intValue());

        // assert
        Assert.assertEquals(output.getNumberValue().intValue(), 0);
    }

    /**
     * Test the process when invalid URL is passed
     *
     * @throws IOException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testMalformedURL() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        DownloadConstruct construct = new DownloadConstruct();
        construct.setWebService(webService);

        // The page
        PageVariable webContext = mock(PageVariable.class);
        MetaExpression webContextVar = mockExpression(ATOMIC);
        when(webContextVar.getMeta(PageVariable.class)).thenReturn(webContext);

        // The URL
        String url = "www.something.com/doc.pdf";
        MetaExpression urlVar = mockExpression(ATOMIC);
        when(urlVar.getStringValue()).thenReturn(url);

        // The target file
        String fileName = "c:/tmp/doc.pdf";
        MetaExpression targetFileVar = mockExpression(ATOMIC);
        when(targetFileVar.getStringValue()).thenReturn(url);
        Path targetFile = mock(Path.class);
        when(TestUtils.CONSTRUCT_FILE_RESOLVER.buildPath(null, targetFileVar)).thenReturn(targetFile);
        TestUtils.setFileResolverReturnValue(targetFile);

        Number timeoutNumber = 5000;
        MetaExpression timeoutVar = mockExpression(ATOMIC);
        when(timeoutVar.getNumberValue()).thenReturn(timeoutNumber);
        when(timeoutVar.getNumberValue().intValue()).thenReturn(timeoutNumber.intValue());

        Mockito.doThrow(new MalformedURLException("")).when(webService).download(url, targetFile, webContext, timeoutNumber.intValue());

        // run
        process(construct, urlVar, targetFileVar, webContextVar, timeoutVar);
    }

    /**
     * Test the process when error occured during downloading
     *
     * @throws IOException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testDownloadFailed() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        DownloadConstruct construct = new DownloadConstruct();
        construct.setWebService(webService);

        // The page
        PageVariable webContext = mock(PageVariable.class);
        MetaExpression webContextVar = mockExpression(ATOMIC);
        when(webContextVar.getMeta(PageVariable.class)).thenReturn(webContext);

        // The URL
        String url = "www.something.com/doc.pdf";
        MetaExpression urlVar = mockExpression(ATOMIC);
        when(urlVar.getStringValue()).thenReturn(url);

        // The target file
        String fileName = "c:/tmp/doc.pdf";
        MetaExpression targetFileVar = mockExpression(ATOMIC);
        when(targetFileVar.getStringValue()).thenReturn(url);
        Path targetFile = mock(Path.class);
        when(TestUtils.CONSTRUCT_FILE_RESOLVER.buildPath(null, targetFileVar)).thenReturn(targetFile);
        TestUtils.setFileResolverReturnValue(targetFile);

        Number timeoutNumber = 5000;
        MetaExpression timeoutVar = mockExpression(ATOMIC);
        when(timeoutVar.getNumberValue()).thenReturn(timeoutNumber);
        when(timeoutVar.getNumberValue().intValue()).thenReturn(timeoutNumber.intValue());

        Mockito.doThrow(new IOException("")).when(webService).download(url, targetFile, webContext, timeoutNumber.intValue());

        // run
        process(construct, urlVar, targetFileVar, webContextVar, timeoutVar);
    }

    /**
     * Test the process when empty URL is passed
     *
     * @throws IOException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testNullURL() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        DownloadConstruct construct = new DownloadConstruct();
        construct.setWebService(webService);
        MetaExpression webContextVar = mockExpression(ATOMIC);
        MetaExpression urlVar = mockExpression(ATOMIC);
        when(urlVar.getStringValue()).thenReturn("");
        MetaExpression targetFileVar = mockExpression(ATOMIC);
        MetaExpression timeoutVar = mockExpression(ATOMIC);

        // run
        process(construct, urlVar, targetFileVar, webContextVar, timeoutVar);
    }

    /**
     * Test the process when empty filename is passed
     *
     * @throws IOException
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid variable value. Filename is empty.")
    public void testNullFilename() throws IOException {
        // mock
        WebService webService = mock(WebService.class);
        DownloadConstruct construct = new DownloadConstruct();
        construct.setWebService(webService);
        MetaExpression webContextVar = mockExpression(ATOMIC);
        MetaExpression urlVar = mockExpression(ATOMIC);
        when(urlVar.getStringValue()).thenReturn("A");
        MetaExpression targetFileVar = mockExpression(ATOMIC);
        when(targetFileVar.getStringValue()).thenReturn("");
        MetaExpression timeoutVar = mockExpression(ATOMIC);

        // run
        process(construct, urlVar, targetFileVar, webContextVar, timeoutVar);
    }
}
