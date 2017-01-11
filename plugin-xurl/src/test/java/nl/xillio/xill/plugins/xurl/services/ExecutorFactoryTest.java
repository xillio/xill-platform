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
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.xurl.data.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
        // See the comments above
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

        Options options = optionsFactory.build(input);
        String id1 = ExecutorFactory.getSessionID(options);
        String id2 = ExecutorFactory.getSessionID(options);

        Assert.assertEquals(id1, id2);
    }


    /**
     * Test ID when insecure option is provided -> only same connection with same options
     */
    @Test
    public void testIDOptionInsecure() {
        Options options1 = optionsFactory.build(createMap("insecure", fromValue(true)));
        Options options2 = optionsFactory.build(createMap("insecure", fromValue(false)));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2); // Should be different from id1

        Assert.assertNotEquals(id1, id2);
    }

    /**
     * Test caching of connection when redirect option is provided -> only same connection with same options
     */
    @Test
    public void testIDOptionRedirect() {
        Options options1 = optionsFactory.build(createMap("enableRedirect", fromValue(true)));
        Options options2 = optionsFactory.build(createMap("enableRedirect", fromValue(false)));

        String id1 = ExecutorFactory.getSessionID(options1);
        String id2 = ExecutorFactory.getSessionID(options2); // Should be different from id1

        Assert.assertNotEquals(id1, id2);
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
        String id2 = ExecutorFactory.getSessionID(options2); // Should be different from id1
        String id3 = ExecutorFactory.getSessionID(options3); // Should be different from id2
        String id4 = ExecutorFactory.getSessionID(options4); // Should be different from id2
        String id5 = ExecutorFactory.getSessionID(options5); // Should be same as id2

        Assert.assertNotEquals(id1, id2);
        Assert.assertNotEquals(id2, id3);
        Assert.assertNotEquals(id2, id4);
        Assert.assertEquals(id2, id5);
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
        String id2 = ExecutorFactory.getSessionID(options2); // Should be Different from id1
        String id3 = ExecutorFactory.getSessionID(options3); // Should be Different from id2
        String id4 = ExecutorFactory.getSessionID(options4); // Should be same as id2

        Assert.assertNotEquals(id1, id2);
        Assert.assertNotEquals(id2, id3);
        Assert.assertEquals(id2, id4);
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
        String id2 = ExecutorFactory.getSessionID(options2); // Should be different from ex1
        String id3 = ExecutorFactory.getSessionID(options3); // Should be different from ex2
        String id4 = ExecutorFactory.getSessionID(options4); // Should be different from ex2
        String id5 = ExecutorFactory.getSessionID(options5); // Should be different from ex2
        String id6 = ExecutorFactory.getSessionID(options6); // Should be same as ex2

        Assert.assertNotEquals(id1, id2);
        Assert.assertNotEquals(id2, id3);
        Assert.assertNotEquals(id2, id4);
        Assert.assertNotEquals(id2, id5);
        Assert.assertEquals(id2, id6);
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

        Assert.assertEquals(ex1a, ex1b);
        Assert.assertEquals(ex2a, ex2b);
        Assert.assertNotEquals(ex1a, ex2a);
    }


    /*
     * Utility functions
     */
    private MetaExpression createMap(String name, MetaExpression value) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put(name, value);
        return fromValue(result);
    }

    private MetaExpression createMap(String name, MetaExpression value, String name2, MetaExpression value2) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put(name, value);
        result.put(name2, value2);
        return fromValue(result);
    }

    private MetaExpression emptyMap() {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        return fromValue(result);
    }


}
