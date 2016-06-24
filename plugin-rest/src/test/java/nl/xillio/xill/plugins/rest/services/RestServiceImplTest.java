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
package nl.xillio.xill.plugins.rest.services;

import nl.xillio.xill.plugins.rest.data.Content;
import nl.xillio.xill.plugins.rest.data.Options;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link RestService}.
 */
public class RestServiceImplTest {

    /**
     * Test the processRequest() method with normal usage
     */
    @Test
    public void testProcessRequest() throws IOException {
        // mock
        Request request = mock(Request.class);
        when(request.connectTimeout(anyInt())).thenReturn(request);
        when(request.socketTimeout(anyInt())).thenReturn(request);

        Options options = mock(Options.class);
        when(options.getTimeout()).thenReturn(3000);
        doNothing().when(options).setHeaders(request);
        doNothing().when(options).setAuth(any());

        Content body = mock(Content.class);
        when(body.isEmpty()).thenReturn(false);
        doNothing().when(body).setToRequest(request);

        Response response = mock(Response.class);
        when(response.returnContent()).thenReturn(null);

        RestServiceImpl implementation = spy(new RestServiceImpl());

        Executor executor = mock(Executor.class);
        doReturn(executor).when(implementation).createExecutor(false);
        when(executor.execute(request)).thenReturn(response);

        // run
        Content content = implementation.processRequest(request, options, body);

        // verify
        verify(implementation, times(1)).createExecutor(false);
        verify(options, times(3)).getTimeout();
        verify(options, times(1)).setHeaders(request);
        verify(options, times(1)).setAuth(any());
        verify(request, times(1)).socketTimeout(anyInt());
        verify(body, times(1)).setToRequest(request);

        // assert
        Assert.assertNotNull(content);
    }

    /**
     * Test the get() method with normal usage
     */
    @Test
    public void testGet() {
        Options options = mock(Options.class);
        RestServiceImpl implementation = spy(new RestServiceImpl());
        doReturn(null).when(implementation).processRequest(any(), any(), any());
        implementation.get("bla", options);
    }

    /**
     * Test the put() method with normal usage
     */
    @Test
    public void testPut() {
        Content body = mock(Content.class);
        Options options = mock(Options.class);
        RestServiceImpl implementation = spy(new RestServiceImpl());
        doReturn(null).when(implementation).processRequest(any(), any(), any());
        implementation.put("bla", options, body);
    }

    /**
     * Test the post() method with normal usage
     */
    @Test
    public void testPost() {
        Content body = mock(Content.class);
        Options options = mock(Options.class);
        RestServiceImpl implementation = spy(new RestServiceImpl());
        doReturn(null).when(implementation).processRequest(any(), any(), any());
        implementation.post("bla", options, body);
    }

    /**
     * Test the delete() method with normal usage
     */
    @Test
    public void testDelete() {
        Options options = mock(Options.class);
        RestServiceImpl implementation = spy(new RestServiceImpl());
        doReturn(null).when(implementation).processRequest(any(), any(), any());
        implementation.delete("bla", options);
    }

    /**
     * Test the head() method with normal usage
     */
    @Test
    public void testHead() {
        Options options = mock(Options.class);
        RestServiceImpl implementation = spy(new RestServiceImpl());
        doReturn(null).when(implementation).processRequest(any(), any(), any());
        implementation.head("bla", options);
    }
}