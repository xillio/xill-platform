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
import nl.xillio.xill.plugins.web.data.Options;
import nl.xillio.xill.plugins.web.data.OptionsFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Test the {@link OptionsFactory}
 */
public class OptionsTest extends TestUtils {

    /**
     * All the names of the options that require a boolean.
     *
     * @return A list of all the names that require a boolean.
     */
    @DataProvider(name = "booleanOptionTest")
    public static Object[][] booleanTestStrings() {
        return new Object[][]{
                {"enablejs", "enablejs"},
                {"enablewebsecurity", "enablewebsecurity"},
                {"loadimages", "loadimages"},
                {"insecuressl", "insecuressl"},
                {"ltrurlaccess", "ltrurlaccess"}};
    }

    /**
     * All the values of proxytype that we accept.
     *
     * @return A list with the name string "proxytype" combined with the accepted value.
     */
    @DataProvider(name = "proxytypes")
    public static Object[][] proxyTypeTest() {
        return new Object[][]{
                {"proxytype", "http"},
                {"proxytype", "socks5"},
                {"proxytype", null}
        };
    }

    /**
     * All the invalud values for a HttpUserPass.
     *
     * @return A list with the name string "pass" combined with invalid values.
     */
    @DataProvider(name = "invalidHttpUserPasses")
    public static Object[][] invalidPasses() {
        return new Object[][]{
                {"pass", null},
                {"pass", ""}
        };
    }

    /**
     * All the options that require a string and their valid values.
     *
     * @return A list with the name string combined with the valid value.
     */
    @DataProvider(name = "stringOptionTest")
    public static Object[][] stringTestStrings() {
        return new Object[][]{
                {"sslprotocol", "sslv3"},
                {"sslprotocol", "sslv2"},
                {"sslprotocol", "tlsv1"},
                {"sslprotocol", "any"},
                {"browser", "PHANTOMJS"}
        };
    }

    /**
     * A test which tests the proxy option with all proxy values.
     *
     * @param proxytypes The name string for the proxytype option
     * @param type       The actual valid type.
     * @throws Exception
     */
    @Test(dataProvider = "proxytypes")
    public void testProxyOption(final String proxytypes, final String type) throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // The proxyHost:
        String proxyHostValue = "option of proxyhost";
        MetaExpression proxyHost = mock(MetaExpression.class);
        when(proxyHost.getStringValue()).thenReturn(proxyHostValue);
        optionsValue.put("proxyhost", proxyHost);

        // The proxy port, needed to make proxyhost run
        int proxyPortValue = 5;
        MetaExpression proxyPort = mock(MetaExpression.class);
        when(proxyPort.getNumberValue()).thenReturn(proxyPortValue);
        optionsValue.put("proxyport", proxyPort);

        // The proxy user
        String proxyUserValue = "value of the proxyUser";
        MetaExpression proxyUser = mock(MetaExpression.class);
        when(proxyUser.getStringValue()).thenReturn(proxyUserValue);
        optionsValue.put("proxyuser", proxyUser);

        // the proxy pass
        String proxyPassValue = "you shall pass";
        MetaExpression proxyPass = mock(MetaExpression.class);
        when(proxyPass.getStringValue()).thenReturn(proxyPassValue);
        optionsValue.put("proxypass", proxyPass);

        // the proxy type
        MetaExpression proxyType = mock(MetaExpression.class);
        when(proxyType.getStringValue()).thenReturn(type);
        optionsValue.put(proxytypes, proxyType);

        // run
        Options output = optionsFactory.processOptions(options);

        // verify
        verify(proxyHost, times(1)).getStringValue();
        verify(proxyPort, times(1)).getNumberValue();
        verify(proxyUser, times(1)).getStringValue();
        verify(proxyPass, times(1)).getStringValue();
        verify(proxyType, times(1)).getStringValue();

        // assert
        Assert.assertEquals(output.getProxyUser(), proxyUserValue);
        Assert.assertEquals(output.getProxyPass(), proxyPassValue);
    }

    /**
     * Tests the proxy option error handling when an invalid proxytype is handed.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid proxy type.")
    public void TestProxyOptionInvalidProxyType() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // The proxyHost:
        String proxyHostValue = "option of proxyhost";
        MetaExpression proxyHost = mock(MetaExpression.class);
        when(proxyHost.getStringValue()).thenReturn(proxyHostValue);
        optionsValue.put("proxyhost", proxyHost);

        // The proxy port, needed to make proxyhost run
        int proxyPortValue = 5;
        MetaExpression proxyPort = mock(MetaExpression.class);
        when(proxyPort.getNumberValue()).thenReturn(proxyPortValue);
        optionsValue.put("proxyport", proxyPort);

        // The proxy user
        String proxyUserValue = "value of the proxyUser";
        MetaExpression proxyUser = mock(MetaExpression.class);
        when(proxyUser.getStringValue()).thenReturn(proxyUserValue);
        optionsValue.put("proxyuser", proxyUser);

        // the proxy pass
        String proxyPassValue = "you shall pass";
        MetaExpression proxyPass = mock(MetaExpression.class);
        when(proxyPass.getStringValue()).thenReturn(proxyPassValue);
        optionsValue.put("proxypass", proxyPass);

        // the proxy type
        String proxyTypeValue = "invalidValue";
        MetaExpression proxyType = mock(MetaExpression.class);
        when(proxyType.getStringValue()).thenReturn(proxyTypeValue);
        optionsValue.put("proxytype", proxyType);

        // run
        optionsFactory.processOptions(options);
    }

    /**
     * Tests the proxy option error handling when no proxyport is handed.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Proxyport must be given in the options OBJECT")
    public void TestProxyNoProxyPort() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // The proxyHost:
        String proxyHostValue = "option of proxyhost";
        MetaExpression proxyHost = mock(MetaExpression.class);
        when(proxyHost.getStringValue()).thenReturn(proxyHostValue);
        optionsValue.put("proxyhost", proxyHost);

        // The proxy user
        String proxyUserValue = "value of the proxyUser";
        MetaExpression proxyUser = mock(MetaExpression.class);
        when(proxyUser.getStringValue()).thenReturn(proxyUserValue);
        optionsValue.put("proxyuser", proxyUser);

        // the proxy pass
        String proxyPassValue = "you shall pass";
        MetaExpression proxyPass = mock(MetaExpression.class);
        when(proxyPass.getStringValue()).thenReturn(proxyPassValue);
        optionsValue.put("proxypass", proxyPass);

        // the proxy type
        String proxyTypeValue = "invalidValue";
        MetaExpression proxyType = mock(MetaExpression.class);
        when(proxyType.getStringValue()).thenReturn(proxyTypeValue);
        optionsValue.put("proxytype", proxyType);

        // run
        optionsFactory.processOptions(options);
    }

    /**
     * Tests the error handling of the proxy option when the proxyUser is set but no proxypass is given or the other way around.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "The Proxyuser and proxypass must either both be set up in the options OBJECT or none of them.")
    public void testProxyOptionIncorrectSetup() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // The proxyHost:
        String proxyHostValue = "option of proxyhost";
        MetaExpression proxyHost = mock(MetaExpression.class);
        when(proxyHost.getStringValue()).thenReturn(proxyHostValue);
        optionsValue.put("proxyhost", proxyHost);

        // The proxy port, needed to make proxyhost run
        int proxyPortValue = 5;
        MetaExpression proxyPort = mock(MetaExpression.class);
        when(proxyPort.getNumberValue()).thenReturn(proxyPortValue);
        optionsValue.put("proxyport", proxyPort);

        // The proxy user
        String proxyUserValue = "value of the proxyUser";
        MetaExpression proxyUser = mock(MetaExpression.class);
        when(proxyUser.getStringValue()).thenReturn(proxyUserValue);
        optionsValue.put("proxyuser", proxyUser);

        // the proxy pass
        MetaExpression proxyPass = mock(MetaExpression.class);
        when(proxyPass.getStringValue()).thenReturn(null);
        optionsValue.put("proxypass", proxyPass);

        // the proxy type
        String proxyTypeValue = "socks5";
        MetaExpression proxyType = mock(MetaExpression.class);
        when(proxyType.getStringValue()).thenReturn(proxyTypeValue);
        optionsValue.put("proxytype", proxyType);

        // run
        optionsFactory.processOptions(options);
    }

    /**
     * Tests the httAuthUser option.
     *
     * @throws Exception
     */
    @Test
    public void testUserOption() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // the user option
        String userValue = "httpAuthUser";
        MetaExpression user = mock(MetaExpression.class);
        when(user.getStringValue()).thenReturn(userValue);
        optionsValue.put("user", user);

        // the pass that belongs with the user
        String passValue = "user may pass";
        MetaExpression pass = mock(MetaExpression.class);
        when(pass.getStringValue()).thenReturn(passValue);
        optionsValue.put("pass", pass);

        Options output = optionsFactory.processOptions(options);

        // verify
        verify(user, times(1)).getStringValue();
        verify(pass, times(1)).getStringValue();

        // assert
        Assert.assertEquals(output.getHttpAuthUser(), userValue);
        Assert.assertEquals(output.getHttpAuthPass(), passValue);
    }

    /**
     * Tests the httpUserOption when an invalid pass is given.
     *
     * @param httpUserpass The name string for the httpUserPass.
     * @param passValue    The value of the pass.
     * @throws Exception
     */
    @Test(dataProvider = "invalidHttpUserPasses", expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Http password must be set if user is used.")
    public void testUserOptionNoHTTPPass(final String httpUserpass, final String passValue) throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // the user option
        String userValue = "httpAuthUser";
        MetaExpression user = mock(MetaExpression.class);
        when(user.getStringValue()).thenReturn(userValue);
        optionsValue.put("user", user);

        // the pass that belongs with the user
        MetaExpression pass = mock(MetaExpression.class);
        when(pass.getStringValue()).thenReturn(passValue);
        optionsValue.put(httpUserpass, pass);

        optionsFactory.processOptions(options);
    }

    /**
     * Tests all the options that require a boolean value. (We default to false).
     *
     * @param name  the name string of the option.
     * @param name2 The name string again (had to hand two values).
     * @throws Exception
     */
    @Test(dataProvider = "booleanOptionTest")
    public void testBooleanOptions(final String name, final String name2) throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression option = mock(MetaExpression.class);
        when(option.getBooleanValue()).thenReturn(false);
        optionsValue.put(name, option);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);

        // verify
        verify(option, times(1)).getBooleanValue();
    }

    /**
     * Tests all the options that require a string value.
     *
     * @param tagName The name string of the option.
     * @param value   The value we want to set the option to.
     * @throws Exception
     */
    @Test(dataProvider = "stringOptionTest")
    public void testStringOptions(final String tagName, final String value) throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression option = mock(MetaExpression.class);
        when(option.getStringValue()).thenReturn(value);
        optionsValue.put(tagName, option);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);

        // verify
        verify(option, times(1)).getStringValue();
    }

    /**
     * Tests the error handling when an invalid sslProtocol is handed.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid sslprotocol.")
    public void testInvalidSslProtocol() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression option = mock(MetaExpression.class);
        when(option.getStringValue()).thenReturn("non existing protocol");
        optionsValue.put("sslprotocol", option);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);
    }

    /**
     * Tests the browser option error handling with an invalid browser given.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid \"browser\" option.")
    public void testInvalidBrowserOption() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression option = mock(MetaExpression.class);
        when(option.getStringValue()).thenReturn("invalid browser");
        optionsValue.put("browser", option);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);
    }

    /**
     * Tests the error handling when an unknown option is trying to be set.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Unknown option: nonExistingOption")
    public void testUnknownOption() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression option = mock(MetaExpression.class);
        when(option.getStringValue()).thenReturn("non existing value");
        optionsValue.put("nonExistingOption", option);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);
    }

    /**
     * Tests the options error handling. when we do not hand an object but an ATOMIC value.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid options variable!")
    public void testNoObjectGiven() throws Exception {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The url
        String urlValue = "This is an url";
        MetaExpression url = mock(MetaExpression.class);
        when(url.getStringValue()).thenReturn(urlValue);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        MetaExpression option = mock(MetaExpression.class);
        when(option.getStringValue()).thenReturn("non existing value");
        optionsValue.put("nonExistingOption", option);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(ATOMIC);

        // run
        optionsFactory.processOptions(options);
    }

    @Test
    public void testResolutionOptions() {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The resolution value
        Number resNum = new Integer(1000); // Valid width and height
        MetaExpression resValue = mock(MetaExpression.class);
        when(resValue.getNumberValue()).thenReturn(resNum);
        List<MetaExpression> list = new LinkedList<>();
        list.add(resValue);
        list.add(resValue);
        MetaExpression value = mock(MetaExpression.class);
        when(value.getType()).thenReturn(LIST);
        when(value.getValue()).thenReturn(list);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        optionsValue.put("resolution", value);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);

        // verify
        verify(options, times(1)).getValue();
        verify(options, times(1)).getType();
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid \"resolution\" option. The minimum resolution is.*")
    public void testNotSupportedResolutionOptions() {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The resolution value
        Number resNum = new Integer(10); // Width and height lower than minimum allowed
        MetaExpression resValue = mock(MetaExpression.class);
        when(resValue.getNumberValue()).thenReturn(resNum);
        List<MetaExpression> list = new LinkedList<>();
        list.add(resValue);
        list.add(resValue);
        MetaExpression value = mock(MetaExpression.class);
        when(value.getType()).thenReturn(LIST);
        when(value.getValue()).thenReturn(list);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        optionsValue.put("resolution", value);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid variable type of \"resolution\" option.*")
    public void testInvalidVariableTypeResolutionOptions() {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The resolution value
        MetaExpression value = mock(MetaExpression.class);
        when(value.getType()).thenReturn(OBJECT);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        optionsValue.put("resolution", value);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = "Invalid \"resolution\" option. Expected value is the list of pixel width and height.")
    public void testInvalidResolutionOptions() {
        // mock
        OptionsFactory optionsFactory = new OptionsFactory();

        // The resolution value
        Number resNum = new Integer(10); // Width and height lower than minimum allowed
        MetaExpression resValue = mock(MetaExpression.class);
        when(resValue.getNumberValue()).thenReturn(resNum);
        List<MetaExpression> list = new LinkedList<>();
        list.add(resValue); // Add just once
        MetaExpression value = mock(MetaExpression.class);
        when(value.getType()).thenReturn(LIST);
        when(value.getValue()).thenReturn(list);

        // The options
        LinkedHashMap<String, MetaExpression> optionsValue = new LinkedHashMap<>();
        optionsValue.put("resolution", value);

        MetaExpression options = mock(MetaExpression.class);
        when(options.getValue()).thenReturn(optionsValue);
        when(options.getType()).thenReturn(OBJECT);

        // run
        optionsFactory.processOptions(options);
    }
}
