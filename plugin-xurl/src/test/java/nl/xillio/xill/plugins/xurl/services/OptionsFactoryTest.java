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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.plugins.xurl.data.ProxyOptions;
import org.apache.http.entity.ContentType;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class OptionsFactoryTest {
    private OptionsFactory optionsFactory = new OptionsFactory();

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testUnknownOption() {
        MetaExpression input = map("I DO NOT EXIST", fromValue(""));
        optionsFactory.build(input);
    }

    @Test
    public void testBasicAuth() {
        MetaExpression auth = map("username", fromValue("MyUser"), "password", fromValue("MyPass123"));
        MetaExpression input = map("basicAuth", auth);

        Options options = optionsFactory.build(input);

        assertEquals(options.getBasicAuth().getUsername(), "MyUser");
        assertEquals(options.getBasicAuth().getPassword(), "MyPass123");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*OBJECT.*")
    public void testBasicAuthNotObject() {
        MetaExpression input = map("basicAuth", fromValue(""));
        optionsFactory.build(input);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*password.*")
    public void testBasicAuthNoPassword() {
        MetaExpression auth = map("username", fromValue("user"));
        MetaExpression input = map("basicAuth", auth);

        optionsFactory.build(input);
    }

    @Test
    public void testProxyHostAndPort() {
        MetaExpression proxy = map("host", fromValue("localhost"), "port", fromValue(1234));
        MetaExpression input = map("proxy", proxy);

        Options options = optionsFactory.build(input);

        assertEquals(options.getProxyOptions().getHttpHost().getHostName(), "localhost");
        assertEquals(options.getProxyOptions().getHttpHost().getPort(), 1234);
    }

    @Test
    public void testProxyWithCredentials() {
        LinkedHashMap<String, MetaExpression> proxyMap = new LinkedHashMap<>();
        proxyMap.put("username", fromValue("IAMUSER"));
        proxyMap.put("password", fromValue("SECURE1"));
        proxyMap.put("host", fromValue("notsolocal"));

        MetaExpression input = map("proxy", fromValue(proxyMap));

        Options options = optionsFactory.build(input);
        ProxyOptions proxyOptions = options.getProxyOptions();
        assertEquals(proxyOptions.getHttpHost().getHostName(), "notsolocal");
        assertEquals(proxyOptions.getUsername(), "IAMUSER");
        assertEquals(proxyOptions.getPassword(), "SECURE1");
    }

    @Test
    public void testTimeout() {
        MetaExpression input = map("timeout", fromValue(5032));
        Options options = optionsFactory.build(input);
        assertEquals(options.getTimeout(), 5032);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*number.*")
    public void testTimeoutNaN() {
        MetaExpression input = map("timeout", fromValue("Hello"));
        optionsFactory.build(input);
    }

    @Test
    public void testHeaders() {
        MetaExpression headers = map("Header1", fromValue("COOL VALUE"), "header-X", fromValue("NOT SO COOL VALUE"));
        MetaExpression input = map("headers", headers);

        Options options = optionsFactory.build(input);

        assertEquals(Arrays.toString(options.getHeaders()), "[Header1: COOL VALUE, header-X: NOT SO COOL VALUE]");
    }

    @Test
    public void testInsecure() {
        MetaExpression input = map("insecure", fromValue(true));
        Options options = optionsFactory.build(input);
        assertTrue(options.isInsecure());
    }

    @Test
    public void testResponseContentType() {
        MetaExpression input = map("responseContentType", fromValue("application/xml"));
        Options options = optionsFactory.build(input);

        assertEquals(options.getResponseContentType().toString(), "application/xml");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Could not parse .*")
    public void testParseContentTypeException() {
        String contentType = "";
        OptionsFactory.parseContentType(contentType);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*supported.*")
    public void testParseContentTypeInvalidCharset() {
        String contentType = "something; charset=PAUL";
        OptionsFactory.parseContentType(contentType);
    }

    @Test
    public void testLogging() {
        MetaExpression input = map("logging", fromValue("info"));

        Options options = optionsFactory.build(input);

        assertEquals(options.getLogging(), "info");
    }


    private MetaExpression map(String name, MetaExpression value) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put(name, value);
        return fromValue(result);
    }

    private MetaExpression map(String name, MetaExpression value, String name2, MetaExpression value2) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        result.put(name, value);
        result.put(name2, value2);
        return fromValue(result);
    }
}