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
package nl.xillio.xill.plugins.xurl.data;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class OptionsTest {

    @Test
    public void testApplyTimeout() {
        Options options = new Options();
        options.setTimeout(5);

        Request request = mock(Request.class);
        options.apply(request);

        verify(request).socketTimeout(5);
        verify(request).connectTimeout(5);
    }

    @Test
    public void testApplyHeaders() {
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "awesomesauce"),
                new BasicHeader("X-Token", "fluh28743tp1j42g8h9u029hg842g24g")
        };

        Request request = mock(Request.class);
        Options options = new Options();
        options.setHeaders(headers);
        options.apply(request);

        verify(request).addHeader(headers[0]);
        verify(request).addHeader(headers[1]);
    }

    @Test
    public void testApplyBasicAuth() {
        Credentials auth = new Credentials("Malganis, I AM A TURTLE", "SuperSafe1");
        Options options = new Options();
        options.setBasicAuth(auth);

        Executor executor = mock(Executor.class);
        options.apply(executor);

        verify(executor).auth(auth.getUsername(), auth.getPassword());
    }

    @Test
    public void testApplyProxyAuth() {
        Credentials auth = new Credentials("Bond. James Bond.", "agent007");
        ProxyOptions proxyOptions = new ProxyOptions(new HttpHost("britain"), auth);
        Options options = new Options();
        options.setProxyOptions(proxyOptions);

        Executor executor = mock(Executor.class);
        options.apply(executor);

        verify(executor).auth(proxyOptions.getHttpHost(), auth.getUsername(), auth.getPassword());
        verify(executor).authPreemptiveProxy(proxyOptions.getHttpHost());
    }

    @Test
    public void testApplyProxyNoAuth() {
        ProxyOptions proxyOptions = new ProxyOptions(new HttpHost("britain"), null);
        Options options = new Options();
        options.setProxyOptions(proxyOptions);

        Executor executor = mock(Executor.class);
        options.apply(executor);

        verify(executor).authPreemptiveProxy(proxyOptions.getHttpHost());
    }

    @Test
    public void testGetBodyContentType() throws Exception {
        Options options = new Options();

        assertFalse(options.getBodyContentType().isPresent());

        ContentType contentType = ContentType.APPLICATION_JSON;
        Header[] headers = new Header[]{new BasicHeader("Content-Type", contentType.toString())};
        options.setHeaders(headers);

        assertEquals(options.getBodyContentType().get().toString(), contentType.toString());

    }
}