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
import nl.xillio.xill.plugins.web.InvalidUrlException;
import nl.xillio.xill.plugins.web.data.Options;
import nl.xillio.xill.plugins.web.data.OptionsFactory;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.data.PhantomJSPool;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.MalformedURLException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link LoadPageConstruct}.
 */
public class LoadPageConstructTest extends TestUtils {

    private WebService webService;
    private LoadPageConstruct construct;
    private OptionsFactory optionsFactory;
    private Options returnedOptions;
    private MetaExpression url;
    private MetaExpression options;
    private static final String URL_VALUE = "This is a url";

    @BeforeMethod
    private void prepareTest() {
        // mock
        webService = mock(WebService.class);
        construct = new LoadPageConstruct();
        construct.setWebService(webService);
        optionsFactory = mock(OptionsFactory.class);
        returnedOptions = mock(Options.class);
        construct.setOptionsFactory(optionsFactory);

        // The url
        url = mockExpression(ATOMIC);
        when(url.getStringValue()).thenReturn(URL_VALUE);

        // The options
        options = mockExpression(OBJECT);
    }

    /**
     * Test the process under normal circumstances.
     *
     * @throws Exception
     */
    @Test
    public void testNormalUsage() throws Exception {

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenReturn(returnedOptions);
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(URL_VALUE);
        when(optionsFactory.processOptions(any())).thenReturn(null);
        // run
        MetaExpression output = process(construct, url, options);

        // verify
        verify(optionsFactory, times(1)).processOptions(options);
        verify(webService, times(1)).getEntityFromPool(any(), any());
        verify(webService, times(1)).getCurrentUrl(pageVariable);

        // assert
        Assert.assertEquals(output.getStringValue(), URL_VALUE);
    }

    /**
     * Test the error handling when we {@link OptionsFactory} fails to process the options.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testFailedToParseOptions() throws Exception {

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenThrow(new RobotRuntimeException("Failed to parse the options."));
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(URL_VALUE);

        // run
        MetaExpression output = process(construct, url, options);
    }

    /**
     * Test the error handling when httpGet returns a MalFormedURLException.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*URL.*")
    public void testMalFormedURL() throws Exception {

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenReturn(returnedOptions);
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(URL_VALUE);
        doThrow(new MalformedURLException()).when(webService).httpGet(pageVariable, URL_VALUE);

        // run
        process(construct, url, options);
    }

    /**
     * Test the error handling when httpGet returns a MalFormedURLException.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*URL.*")
    public void testInvalidURLException() throws Exception {

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenReturn(returnedOptions);
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(URL_VALUE);
        doThrow(new InvalidUrlException("Invalid URL: " + URL_VALUE)).when(webService).httpGet(pageVariable, URL_VALUE);

        // run
        process(construct, url, options);
    }
}
