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
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.plugins.xurl.data.ProxyOptions;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class OptionsFactoryTest extends TestUtils {
    private final String USERNAME = "user";
    private final String PASSWORD = "password";
    private final String WORKSTATION = "workstation";
    private final String DOMAIN = "domain.com";
    private OptionsFactory optionsFactory = new OptionsFactory();

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testUnknownOption() {
        MetaExpression input = createMap("I DO NOT EXIST", fromValue(""));
        optionsFactory.build(input);
    }

    @Test
    public void testBasicAuth() {
        MetaExpression auth = createMap("username", fromValue("MyUser"), "password", fromValue("MyPass123"));
        MetaExpression input = createMap("basicAuth", auth);

        Options options = optionsFactory.build(input);

        assertEquals(options.getBasicAuth().getUsername(), "MyUser");
        assertEquals(options.getBasicAuth().getPassword(), "MyPass123");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*OBJECT.*")
    public void testBasicAuthNotObject() {
        MetaExpression input = createMap("basicAuth", fromValue(""));
        optionsFactory.build(input);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*password.*")
    public void testBasicAuthNoPassword() {
        MetaExpression auth = createMap("username", fromValue("user"));
        MetaExpression input = createMap("basicAuth", auth);

        optionsFactory.build(input);
    }

    @Test
    public void testProxyHostAndPort() {
        MetaExpression proxy = createMap("host", fromValue("localhost"), "port", fromValue(1234));
        MetaExpression input = createMap("proxy", proxy);

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

        MetaExpression input = createMap("proxy", fromValue(proxyMap));

        Options options = optionsFactory.build(input);
        ProxyOptions proxyOptions = options.getProxyOptions();
        assertEquals(proxyOptions.getHttpHost().getHostName(), "notsolocal");
        assertEquals(proxyOptions.getUsername(), "IAMUSER");
        assertEquals(proxyOptions.getPassword(), "SECURE1");
    }

    @Test
    public void testTimeout() {
        MetaExpression input = createMap("timeout", fromValue(5032));
        Options options = optionsFactory.build(input);
        assertEquals(options.getTimeout(), 5032);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*number.*")
    public void testTimeoutNaN() {
        MetaExpression input = createMap("timeout", fromValue("Hello"));
        optionsFactory.build(input);
    }

    @Test
    public void testHeaders() {
        MetaExpression headers = createMap("Header1", fromValue("COOL VALUE"), "header-X", fromValue("NOT SO COOL VALUE"));
        MetaExpression input = createMap("headers", headers);

        Options options = optionsFactory.build(input);

        assertEquals(Arrays.toString(options.getHeaders()), "[Header1: COOL VALUE, header-X: NOT SO COOL VALUE]");
    }

    @Test
    public void testIgnoreConnectionCache() throws Exception {
        MetaExpression input = createMap("ignoreConnectionCache", fromValue(true));
        Options options = optionsFactory.build(input);
        assertEquals(options.isIgnoreConnectionCache(), true);
    }

    @Test
    public void testInsecure() {
        MetaExpression input = createMap("insecure", fromValue(true));
        Options options = optionsFactory.build(input);
        assertTrue(options.isInsecure());
    }

    @Test
    public void testMultipart() {
        MetaExpression input = createMap("multipart", fromValue(true));
        Options options = optionsFactory.build(input);
        assertTrue(options.isMultipart());
    }

    @Test
    public void testEnableRedirect() {
        MetaExpression input = createMap("enableRedirect", fromValue(true));
        Options options = optionsFactory.build(input);
        assertTrue(options.isEnableRedirect());
    }

    @Test
    public void testResponseContentType() {
        MetaExpression input = createMap("responseContentType", fromValue("application/xml"));
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
        MetaExpression input = createMap("logging", fromValue("info"));

        Options options = optionsFactory.build(input);

        assertEquals(options.getLogging(), "info");
    }

    @Test
    public void testNTLM() {
        MetaExpression input = createNTLMObject(USERNAME, PASSWORD, WORKSTATION, DOMAIN);
        Options options = optionsFactory.build(input);

        assertEquals(options.getNTLMOptions().getUsername(), USERNAME);
        assertEquals(options.getNTLMOptions().getPassword(), PASSWORD);
        assertEquals(options.getNTLMOptions().getWorkstation(), WORKSTATION);
        assertEquals(options.getNTLMOptions().getDomain(), DOMAIN);
    }

    @Test
    public void testRemoveAcceptEncoding() {
        MetaExpression option = createMap("disableAcceptEncoding", true);

        Options options = optionsFactory.build(option);

        assertTrue(options.isRemoveAcceptEncoding());
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*OBJECT.*")
    public void testNTLMNotObject() {
        MetaExpression input = createMap("ntlm", fromValue(""));
        optionsFactory.build(input);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*username.*")
    public void testNTLMNoUsername() {
        MetaExpression input = createNTLMObject(null, PASSWORD, WORKSTATION, DOMAIN);

        optionsFactory.build(input);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*password.*")
    public void testNTLMNoPassword() {
        MetaExpression input = createNTLMObject(USERNAME, null, WORKSTATION, DOMAIN);

        optionsFactory.build(input);
    }

    @Test()
    public void testNTLMNoWorkstation() {
        // Null value
        MetaExpression input = createNTLMObject(USERNAME, PASSWORD, null, DOMAIN);

        optionsFactory.build(input);

        // Entry not provided at all
        MetaExpression input2 = createNTLMObjectNoWorkspace(USERNAME, PASSWORD, DOMAIN);

        optionsFactory.build(input2);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*domain.*")
    public void testNTLMNoDomain() {
        // Null value
        MetaExpression input = createNTLMObject(USERNAME, PASSWORD, WORKSTATION, null);

        optionsFactory.build(input);

        // Entry not provided at all
        MetaExpression input2 = createNTLMObjectNoDomain(USERNAME, PASSWORD, WORKSTATION);

        optionsFactory.build(input2);
    }

    private MetaExpression createNTLMObjectNoDomain(String username, String password, String workspace) {
        MetaExpression auth = createMap(
                "username", fromValue(username),
                "password", fromValue(password),
                "workspace", fromValue(workspace)
        );
        MetaExpression input = createMap("ntlm", auth);
        return input;
    }

    private MetaExpression createNTLMObjectNoWorkspace(String username, String password, String domain) {
        MetaExpression auth = createMap(
                "username", fromValue(username),
                "password", fromValue(password),
                "domain", fromValue(domain)
        );
        MetaExpression input = createMap("ntlm", auth);
        return input;
    }

    private MetaExpression createNTLMObject(String username, String password, String workstation, String domain) {
        MetaExpression auth = createMap(
                "username", fromValue(username),
                "password", fromValue(password),
                "workstation", fromValue(workstation),
                "domain", fromValue(domain)
        );
        MetaExpression input = createMap("ntlm", auth);
        return input;
    }
}
