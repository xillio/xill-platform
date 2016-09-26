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

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.Options;
import nl.xillio.xill.plugins.web.data.OptionsFactory;
import nl.xillio.xill.plugins.web.data.PageVariable;
import nl.xillio.xill.plugins.web.data.PhantomJSPool;
import nl.xillio.xill.plugins.web.services.web.WebService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.MalformedURLException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link LoadPageConstruct}.
 */
public class LoadPageConstructTest extends ExpressionBuilderHelper {

    /**
     * Test the process under normal circumstances.
     *
     * @throws Exception
     */
    @Test
    public void testNormalUsage() throws Exception {

        // mock
        WebService webService = mock(WebService.class);
        OptionsFactory optionsFactory = mock(OptionsFactory.class);
        Options returnedOptions = mock(Options.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        MetaExpression options = mock(MetaExpression.class);

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenReturn(returnedOptions);
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(urlValue);

        // run
        MetaExpression output = LoadPageConstruct.process(url, options, optionsFactory, webService, pool);

        // verify
        verify(optionsFactory, times(1)).processOptions(options);
        verify(webService, times(1)).getEntityFromPool(any(), any());
        verify(webService, times(1)).getCurrentUrl(pageVariable);

        // assert
        Assert.assertEquals(output.getStringValue(), urlValue);
    }

    /**
     * Test the error handling when we {@link OptionsFactory} fails to process the options.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testFailedToParseOptions() throws Exception {

        // mock
        WebService webService = mock(WebService.class);
        OptionsFactory optionsFactory = mock(OptionsFactory.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        MetaExpression options = mock(MetaExpression.class);

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenThrow(new RobotRuntimeException("Failed to parse the options."));
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(urlValue);

        // run
        MetaExpression output = LoadPageConstruct.process(url, options, optionsFactory, webService, pool);
    }

    /**
     * Test the error handling when httpGet returns a MalFormedURLException.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*URL.*")
    public void testMalFormedURL() throws Exception {

        // mock
        WebService webService = mock(WebService.class);
        OptionsFactory optionsFactory = mock(OptionsFactory.class);
        Options returnedOptions = mock(Options.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        MetaExpression options = mock(MetaExpression.class);

        // the process
        // There is currently no other way to do this; we have to sort out the functions one day.
        when(optionsFactory.processOptions(options)).thenReturn(returnedOptions);
        PageVariable pageVariable = mock(PageVariable.class);
        PhantomJSPool.Entity entity = mock(PhantomJSPool.Entity.class);
        when(webService.getEntityFromPool(any(), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);
        when(webService.getCurrentUrl(pageVariable)).thenReturn(urlValue);
        doThrow(new MalformedURLException()).when(webService).httpGet(pageVariable, urlValue);

        // run
        LoadPageConstruct.process(url, options, optionsFactory, webService, pool);
    }
}
