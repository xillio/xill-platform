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
package nl.xillio.xill.plugins.xurl.services;

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.plugins.xurl.data.Options;
import org.apache.http.client.fluent.Executor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

public class ExecutorFactoryTest extends TestUtils {
    private final ExecutorFactory executorFactory = new ExecutorFactory();
    private final OptionsFactory optionsFactory = new OptionsFactory();

    /*
     * Option Tests
     * A number of options are directly applied while building the executor. Test that this happens correctly.
     */

    /**
     * Test that insecure option is used when provided
     */
    @Test
    public void testOptionInsecure() {

        // Unfortunately Mockito does not officially support mocking final methods, which in this case would be the only
        // way to properly test this method as Apache has made the method final.
        //
        // Mockito 2 can in fact handle this but it uses alfa functionality, see:
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mock-the-unmockable-opt-in-mocking-of-final-classesmethods

        /*
        // Baseline: when false, ssl settings should not be changed
        HttpClientBuilder builder1 = mock(HttpClientBuilder.class);
        Options options1 = mock(Options.class);
        when(options1.isInsecure()).thenReturn(false);
        ExecutorFactory.buildClient(builder1, options1);
        verify(builder1, times(0)).setSSLHostnameVerifier(any());

        // When true, ssl settings should be changed
        HttpClientBuilder builder2 = mock(HttpClientBuilder.class);
        Options options2 = mock(Options.class);
        when(options2.isInsecure()).thenReturn(true);
        ExecutorFactory.buildClient(builder2, options2);
        verify(builder2).setSSLHostnameVerifier(any());
        */
    }

    /**
     * Test that redirect option is used when provided
     */
    @Test
    public void testOptionRedirect() {
        // See the comments regarding mocking final methods above

        /*
        // Baseline: when false, redirect settings should not be changed
        HttpClientBuilder builder1 = mock(HttpClientBuilder.class);
        Options options1 = mock(Options.class);
        when(options1.isEnableRedirect()).thenReturn(true);
        ExecutorFactory.buildClient(builder1, options1);
        verify(builder1, times(0)).disableRedirectHandling();

        // When true, redirect settings should be changed
        HttpClientBuilder builder2 = mock(HttpClientBuilder.class);
        Options options2 = mock(Options.class);
        when(options2.isEnableRedirect()).thenReturn(false);
        ExecutorFactory.buildClient(builder2, options2);
        verify(builder2).disableRedirectHandling();
        */
    }



    /*
     * UUID Tests
     * The following tests verify that we get a unique session UUID when the options change such that a new session will
     * be required
     */

    /**
     * Test ID when no options are provided -> always same connection
      */
    @Test
    public void testIDBase() {
        MetaExpression input = emptyMap();

        Options options1 = optionsFactory.build(input);
        Options options2 = optionsFactory.build(input);
        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2);

        Assert.assertEquals(id1, id2); // Two same inputs must yield same ID
    }


    /**
     * Test ID when insecure option is provided -> only same connection with same options
     */
    @Test
    public void testIDOptionInsecure() {
        Options options1 = optionsFactory.build(createMap("insecure", fromValue(true)));
        Options options2 = optionsFactory.build(createMap("insecure", fromValue(false)));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2);

        Assert.assertNotEquals(id1, id2); // Insecure toggle should yield different ID
    }

    /**
     * Test caching of connection when redirect option is provided -> only same connection with same options
     */
    @Test
    public void testIDOptionRedirect() {
        Options options1 = optionsFactory.build(createMap("enableRedirect", fromValue(true)));
        Options options2 = optionsFactory.build(createMap("enableRedirect", fromValue(false)));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2);

        Assert.assertNotEquals(id1, id2); // Redirect toggle should yield different ID
    }

    /**
     * Test caching of connection when ntlm option is provided -> only same connection with same options
     */
    @Test
    public void testIDOptionNTLM() {
        Options options1 = optionsFactory.build(emptyMap());
        Options options2 = optionsFactory.build(createMap("ntlm", createMap(
                "username", fromValue("user"),
                "password", fromValue("pass"),
                "workstation", fromValue("workstation"),
                "domain", fromValue("domain.com")
        )));
        Options options3 = optionsFactory.build(createMap("ntlm", createMap(
                "username", fromValue("user2"),
                "password", fromValue("pass"),
                "workstation", fromValue("workstation"),
                "domain", fromValue("domain.com")
        )));
        Options options4 = optionsFactory.build(createMap("ntlm", createMap(
                "username", fromValue("user"),
                "password", fromValue("pass"),
                "workstation", fromValue("workstation"),
                "domain", fromValue("domain2.com")
        )));
        Options options5 = optionsFactory.build(createMap("ntlm", createMap(
                "username", fromValue("user"),
                "password", fromValue("pass2"),
                "workstation", fromValue("workstation2"),
                "domain", fromValue("domain.com")
        )));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2);
        String id3 = ExecutorFactory.getSessionID(options3);
        String id4 = ExecutorFactory.getSessionID(options4);
        String id5 = ExecutorFactory.getSessionID(options5);

        Assert.assertNotEquals(id1, id2); // NTLM options should yield different ID from plain request
        Assert.assertNotEquals(id2, id3); // Different user should yield different ID
        Assert.assertNotEquals(id2, id4); // Different domain should yield different ID
        Assert.assertEquals(id2, id5);    // Different password or workstation should yield same ID
    }

    /**
     * Test caching of connection when basicauth option is provided -> only same connection with same options
     */
    @Test
    public void testCachingOptionBasicAuth() {
        Options options1 = optionsFactory.build(emptyMap());
        Options options2 = optionsFactory.build(createMap("basicAuth", createMap(
                "username", fromValue("user"),
                "password", fromValue("pass")
        )));
        Options options3 = optionsFactory.build(createMap("basicAuth", createMap(
                "username", fromValue("user2"),
                "password", fromValue("pass")
        )));
        Options options4 = optionsFactory.build(createMap("basicAuth", createMap(
                "username", fromValue("user"),
                "password", fromValue("pass2")
        )));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2);
        String id3 = ExecutorFactory.getSessionID(options3);
        String id4 = ExecutorFactory.getSessionID(options4);

        Assert.assertNotEquals(id1, id2); // Basic auth should yield different ID from plain request
        Assert.assertNotEquals(id2, id3); // Different user should yield different ID
        Assert.assertEquals(id2, id4);    // Different password should yield same ID
    }

    /**
     * Test caching of connection when proxy option is provided -> only same connection with same options
     */
    @Test
    public void testCachingOptionProxy() {
        Options options1 = optionsFactory.build(emptyMap());
        Options options2 = optionsFactory.build(createMap("proxy", createMap(
                "host", fromValue("domain.com"),
                "port", fromValue(8080),
                "username", fromValue("user"),
                "password", fromValue("pass")
        )));
        Options options3 = optionsFactory.build(createMap("proxy", createMap(
                "host", fromValue("domain2.com"),
                "port", fromValue(8080),
                "username", fromValue("user"),
                "password", fromValue("pass")
        )));
        Options options4 = optionsFactory.build(createMap("proxy", createMap(
                "host", fromValue("domain.com"),
                "port", fromValue(9090),
                "username", fromValue("user"),
                "password", fromValue("pass")
        )));
        Options options5 = optionsFactory.build(createMap("proxy", createMap(
                "host", fromValue("domain.com"),
                "port", fromValue(8080),
                "username", fromValue("user2"),
                "password", fromValue("pass")
        )));
        Options options6 = optionsFactory.build(createMap("proxy", createMap(
                "host", fromValue("domain.com"),
                "port", fromValue(8080),
                "username", fromValue("user"),
                "password", fromValue("pass2")
        )));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2);
        String id3 = ExecutorFactory.getSessionID(options3);
        String id4 = ExecutorFactory.getSessionID(options4);
        String id5 = ExecutorFactory.getSessionID(options5);
        String id6 = ExecutorFactory.getSessionID(options6);

        Assert.assertNotEquals(id1, id2); // Proxy should yield different ID from plain request
        Assert.assertNotEquals(id2, id3); // Different domain should yield different ID
        Assert.assertNotEquals(id2, id4); // Different port should yield different ID
        Assert.assertNotEquals(id2, id5); // Different user should yield different ID
        Assert.assertEquals(id2, id6);    // Different password should yield same ID
    }

    /*
     * Builder Tests
     * Test that the builder
     */

    /**
     * Test that a valid builder is created
     */
    @Test
    public void testDefaultBuilder() {
        Assert.assertNotNull(ExecutorFactory.defaultBuilder());
    }

    /*
     * Caching Tests
     * The following tests verify that the caching mechanism is returning an existing session when required as well as
     * creating a new session when the options have changed such that a new connection is required.
     */
    /**
     * Test that connections with same id are cached
     */
    @Test
    public void testCaching() {
        Options options1 = optionsFactory.build(emptyMap());
        Options options2 = optionsFactory.build(createMap("enableRedirect", fromValue(false)));

        Executor ex1a = executorFactory.buildExecutor(options1);
        Executor ex1b = executorFactory.buildExecutor(options1);
        Executor ex2a = executorFactory.buildExecutor(options2);
        Executor ex2b = executorFactory.buildExecutor(options2);

        Assert.assertEquals(ex1a, ex1b);    // Same plain request should result in same session
        Assert.assertEquals(ex2a, ex2b);    // Same advanced request should result in same session
        Assert.assertNotEquals(ex1a, ex2a); // Plain and advanced request should result in different session
    }


    /*
     * Utility functions
     */

    private MetaExpression emptyMap() {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        return fromValue(result);
    }


}
