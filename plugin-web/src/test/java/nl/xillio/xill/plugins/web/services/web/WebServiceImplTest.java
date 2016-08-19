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
package nl.xillio.xill.plugins.web.services.web;


import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.InvalidUrlException;
import nl.xillio.xill.plugins.web.data.*;
import nl.xillio.xill.plugins.web.data.PhantomJSPool.Entity;
import nl.xillio.xill.plugins.web.data.PhantomJSPool.Identifier;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test the {@link WebService}.
 */
public class WebServiceImplTest {

    /**
     * Test the click function.
     */
    @Test
    public void testClick() {
        // mock
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.click(webVariable);

        // verify
        verify(element, times(1)).click();
    }

    /**
     * Test the getTagname function.
     */
    @Test
    public void testGetTagName() {
        // mock
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);
        when(element.getTagName()).thenReturn("tag!");

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String tagName = implementation.getTagName(webVariable);


        // verify
        verify(element, times(1)).getTagName();

        // assert
        Assert.assertEquals(tagName, "tag!");
    }

    /**
     * Test the getAttribute function
     */
    @Test
    public void testGetAttribute() {
        // mock
        String attributeName = "name";
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);
        when(element.getAttribute(attributeName)).thenReturn("a tribute");

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String attributeValue = implementation.getAttribute(webVariable, attributeName);

        // verify
        verify(element, times(1)).getAttribute(attributeName);

        // assert
        Assert.assertEquals(attributeValue, "a tribute");
    }

    /**
     * @return Returns both {@link WebVariable} types.
     */
    @DataProvider(name = "BothWebTypes")
    private Object[][] getBothWebTypes() {
        return new Object[][]{
                {mock(PageVariable.class), mock(PageVariable.class)},
                {mock(NodeVariable.class), mock(NodeVariable.class)}
        };
    }

    /**
     * Test the getText function for NODE variable.
     */
    @Test
    public void testGetTextNode() {
        String text = "just some text";

        // mock
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);
        when(element.getText()).thenReturn(text);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String tagName = implementation.getText(webVariable);

        // verify
        verify(element, times(1)).getText();

        // assert
        Assert.assertEquals(tagName, text);
    }

    /**
     * Test the getText function for PAGE variable.
     */
    @Test
    public void testGetTextPage() {
        String text = "just some text";

        // mock
        WebElement element = mock(WebElement.class);
        PageVariable pageVariable = mock(PageVariable.class);

        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);
        when(driver.findElement(any())).thenReturn(element);
        when(element.getText()).thenReturn(text);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String tagName = implementation.getText(pageVariable);

        // verify
        verify(element, times(1)).getText();
        verify(driver, times(1)).findElement(any());

        // assert
        Assert.assertEquals(tagName, text);
    }

    /**
     * Test the getText function for PAGE variable when the body tag is not found.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testGetTextPageException() {
        // mock
        PageVariable pageVariable = mock(PageVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);
        when(driver.findElement(any())).thenThrow(new NoSuchElementException("Element not found"));

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.getText(pageVariable);
    }

    /**
     * Tests if we throw a robotruntimeException when Selenium fails to click an element.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testFailingToClick() {
        // mock
        NodeVariable nodeVariable = mock(NodeVariable.class);
        WebElement element = mock(WebElement.class);
        when(nodeVariable.getElement()).thenReturn(element);
        doThrow(new StaleElementReferenceException("")).when(element).click();

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.click(nodeVariable);
    }

    /**
     * Tests if we throw a robotRuntimeException when encountering a selenium error when finding an element with a cssPath.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testInvalidSelectionWhenFindingWithCssPath() {
        // mock
        NodeVariable nodeVariable = mock(NodeVariable.class);
        WebElement element = mock(WebElement.class);
        when(nodeVariable.getElement()).thenReturn(element);

        doThrow(new InvalidSelectorException("")).when(element).findElements(any());
        //
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.findElementsWithCssPath(nodeVariable, "randomPath");
    }

    /**
     * Test if we throw a robotRuntimeException when selenium encounters an invalid state.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testInvalidElementStateWhenFindingWithCssPath() {
        // mock
        NodeVariable nodeVariable = mock(NodeVariable.class);
        WebElement element = mock(WebElement.class);
        when(nodeVariable.getElement()).thenReturn(element);

        doThrow(new InvalidElementStateException("")).when(element).findElements(any());
        //
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.findElementsWithCssPath(nodeVariable, "randomPath");
    }

    /**
     * Test if we throw a RobotRuntimeException when selenium encounters a InvalidSelectorException
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testInvalidSelectorExceptionWhenFindingWithXPath() {
        NodeVariable nodeVariable = mock(NodeVariable.class);
        WebElement webElement = mock(WebElement.class);
        doThrow(new InvalidSelectorException("")).when(webElement).findElements(any());
        when(nodeVariable.getElement()).thenReturn(webElement);

        WebServiceImpl implementation = new WebServiceImpl();
        implementation.findElementsWithXpath(nodeVariable, "anyString");
    }

    /**
     * Test the getSource function.
     */
    @Test
    public void testGetSource() {
        String html = "HTML source";

        // mock
        PageVariable pageVariable = mock(PageVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);
        when(driver.getPageSource()).thenReturn(html);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String source = implementation.getSource(pageVariable);

        // assert
        Assert.assertEquals(source, html);
    }

    /**
     * Test the findElementsWithCssPath function.
     *
     * @param webvar the webVariable (we try both types).
     * @param fodder fodder to match testNG convention.
     */
    @Test(dataProvider = "BothWebTypes")
    public void testFindElementsWithCssPath(WebVariable webvar, WebVariable fodder) {
        // mock
        WebVariable webVariable = webvar;
        WebElement element = mock(WebElement.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getElement()).thenReturn(element);
        when(webVariable.getDriver()).thenReturn(driver);

        //the result of the search
        List<WebElement> searchResults = new ArrayList<>();
        searchResults.add(element);
        when(element.findElements(any())).thenReturn(searchResults);
        when(driver.findElements(any())).thenReturn(searchResults);

        // run
        // run
        WebServiceImpl implementation = new WebServiceImpl();
        List<WebVariable> output = implementation.findElementsWithCssPath(webVariable, "csspath");

        // assert
        Assert.assertEquals(output.size(), 1);
        Assert.assertEquals(output.get(0).getElement(), element);
    }

    /**
     * Test the findElementsWithXPath function.
     *
     * @param webvar the webVariable (we try both types).
     * @param fodder fodder to match testNG convention.
     */
    @Test(dataProvider = "BothWebTypes")
    public void testFindElementsWithxPath(WebVariable webvar, WebVariable fodder) {
        // mock
        WebVariable webVariable = webvar;
        WebElement element = mock(WebElement.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getElement()).thenReturn(element);
        when(webVariable.getDriver()).thenReturn(driver);

        //the result of the search
        List<WebElement> searchResults = new ArrayList<>();
        searchResults.add(element);
        when(element.findElements(any())).thenReturn(searchResults);
        when(driver.findElements(any())).thenReturn(searchResults);

        // run
        // run
        WebServiceImpl implementation = new WebServiceImpl();
        List<WebVariable> output = implementation.findElementsWithXpath(webVariable, "csspath");

        // assert
        Assert.assertEquals(output.size(), 1);
        Assert.assertEquals(output.get(0).getElement(), element);
    }

    /**
     * Test the getCurrentUrl function.
     */
    @Test
    public void testGetCurrentUrl() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getDriver()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn("url");

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String url = implementation.getCurrentUrl(webVariable);


        // verify
        verify(driver, times(1)).getCurrentUrl();

        // assert
        Assert.assertEquals(url, "url");
    }

    /**
     * Test the clear function.
     */
    @Test
    public void testClear() {
        // mock
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.clear(webVariable);

        // verify
        verify(element, times(1)).clear();
    }

    /**
     * Test the sendKeys function.
     *
     * @throws Exception
     */
    @Test
    public void testSendKeys() throws Exception {
        // mock
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.sendKeys(webVariable, "key");

        // verify
        verify(element, times(1)).sendKeys("key");
    }

    /**
     * Test the getTitle function.
     */
    @Test
    public void testGetTitle() {
        // mock
        WebDriver driver = mock(WebDriver.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getDriver()).thenReturn(driver);
        when(driver.getTitle()).thenReturn("title");

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        String titleName = implementation.getTitle(webVariable);

        // verify
        verify(driver, times(1)).getTitle();

        // assert
        Assert.assertEquals(titleName, "title");
    }

    /**
     * Test the getCookies function
     */
    @Test
    public void testGetCookies() {
        // mock
        WebDriver driver = mock(WebDriver.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getDriver()).thenReturn(driver);

        Set<Cookie> resultValue = new HashSet<>();
        resultValue.add(new Cookie("name", "something"));
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn((org.openqa.selenium.WebDriver.Options) options);
        when(options.getCookies()).thenReturn(resultValue);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        Set<Cookie> output = implementation.getCookies(webVariable);

        // verify
        verify(driver, times(1)).manage();
        verify(options, times(1)).getCookies();

        // assert
        Assert.assertEquals(output, resultValue);
    }

    /**
     * Test the deleteCookieNamed function.
     */
    @Test
    public void testDeleteCookieNamed() {
        // mock
        WebDriver driver = mock(WebDriver.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getDriver()).thenReturn(driver);

        //the options for manage()
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.deleteCookieNamed(webVariable, "name");

        // verify
        verify(driver, times(1)).manage();
        verify(options, times(1)).deleteCookieNamed("name");
    }

    /**
     * Test the deleteCookiesfunction.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteCookies() throws Exception {
        // mock
        WebDriver driver = mock(WebDriver.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getDriver()).thenReturn(driver);

        //the options for manage()
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.deleteCookies(webVariable);

        // verify
        verify(driver, times(1)).manage();
        verify(options, times(1)).deleteAllCookies();
    }

    /**
     * Test the getScreenshotAsFile function
     */
    @Test
    public void testGetScreenshotAsFile() {
        WebServiceImpl implementation = spy(new WebServiceImpl());
        // mock
        WebDriver driver = mock(WebDriver.class);
        WebVariable webVariable = mock(WebVariable.class);
        File outPutFile = mock(File.class);
        when(webVariable.getDriver()).thenReturn(driver);

        //the options for manage()
        PhantomJSDriver jsdriver = mock(PhantomJSDriver.class);
        doReturn(jsdriver).when(implementation).getJSDriver(driver);
        doReturn(outPutFile).when(jsdriver).getScreenshotAs(any());

        // run
        implementation.getScreenshotAsFilePath(webVariable, 0, 0); // Default resolution

        // verify
        verify(jsdriver, times(1)).getScreenshotAs(any());
    }

    /**
     * Test the getScreenshotAsFile function
     */
    @Test
    public void testGetScreenshotAsFileSpecificDifferentResolution() {
        WebServiceImpl implementation = spy(new WebServiceImpl());
        // mock
        WebDriver driver = mock(WebDriver.class);

        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        Dimension resolution = new Dimension(1600, 900);
        when(window.getSize()).thenReturn(resolution);

        WebVariable webVariable = mock(WebVariable.class);
        File outPutFile = mock(File.class);
        when(webVariable.getDriver()).thenReturn(driver);

        //the options for manage()
        PhantomJSDriver jsdriver = mock(PhantomJSDriver.class);
        doReturn(jsdriver).when(implementation).getJSDriver(driver);
        doReturn(outPutFile).when(jsdriver).getScreenshotAs(any());

        // run
        implementation.getScreenshotAsFilePath(webVariable, 1000, 1000);

        // verify
        verify(jsdriver, times(1)).getScreenshotAs(any());
    }

    /**
     * Test the getScreenshotAsFile function
     */
    @Test
    public void testGetScreenshotAsFileSpecificSameResolution() {
        WebServiceImpl implementation = spy(new WebServiceImpl());
        // mock
        WebDriver driver = mock(WebDriver.class);

        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        Dimension resolution = new Dimension(1600, 900);
        when(window.getSize()).thenReturn(resolution);

        WebVariable webVariable = mock(WebVariable.class);
        File outPutFile = mock(File.class);
        when(webVariable.getDriver()).thenReturn(driver);

        //the options for manage()
        PhantomJSDriver jsdriver = mock(PhantomJSDriver.class);
        doReturn(jsdriver).when(implementation).getJSDriver(driver);
        doReturn(outPutFile).when(jsdriver).getScreenshotAs(any());

        // run
        implementation.getScreenshotAsFilePath(webVariable, 1600, 900);

        // verify
        verify(jsdriver, times(1)).getScreenshotAs(any());
    }

    /**
     * Test the isSelected function
     */
    @Test
    public void testIsSelected() {
        // mock
        boolean isSelected = true;
        WebElement element = mock(WebElement.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getElement()).thenReturn(element);
        when(element.isSelected()).thenReturn(isSelected);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        boolean output = implementation.isSelected(webVariable);

        // verify
        verify(element, times(1)).isSelected();

        // assert
        Assert.assertEquals(output, isSelected);
    }


    /**
     * Test the switchToFrame function with an element given
     */
    @Test
    public void testSwitchToElementFrame() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getDriver()).thenReturn(driver);

        TargetLocator locator = mock(TargetLocator.class);
        when(driver.switchTo()).thenReturn(locator);

        WebElement element = mock(WebElement.class);
        WebVariable frameVariable = mock(WebVariable.class);
        when(frameVariable.getElement()).thenReturn(element);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.switchToFrame(webVariable, frameVariable);

        // verify
        verify(driver, times(1)).switchTo();
        verify(locator, times(1)).frame(element);
    }

    /**
     * Test the switchToFrame function with an integer given
     */
    @Test
    public void testSwitchToIntegerFrame() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getDriver()).thenReturn(driver);

        TargetLocator locator = mock(TargetLocator.class);
        when(driver.switchTo()).thenReturn(locator);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.switchToFrame(webVariable, 42);

        // verify
        verify(driver, times(1)).switchTo();
        verify(locator, times(1)).frame(42);
    }

    /**
     * Test the switchToFrame function with an integer given
     */
    @Test
    public void testSwitchToStringFrame() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getDriver()).thenReturn(driver);

        TargetLocator locator = mock(TargetLocator.class);
        when(driver.switchTo()).thenReturn(locator);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.switchToFrame(webVariable, "string");

        // verify
        verify(driver, times(1)).switchTo();
        verify(locator, times(1)).frame("string");
    }

    /**
     * Test the addCookie function
     */
    @Test
    public void testAddCookie() {
        // mock
        WebVariable webVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getDriver()).thenReturn(driver);

        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);

        CookieVariable cookieVariable = mock(CookieVariable.class);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.addCookie(webVariable, cookieVariable);

        // verify
        verify(driver, times(1)).manage();
        verify(options, times(1)).addCookie(any());
    }

    /**
     * Test the createNodeVariable function.
     */
    @Test
    public void createNodeVariable() {
        // mock
        WebVariable pageVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);

        WebElement element = mock(WebElement.class);
        WebVariable elementVariable = mock(WebVariable.class);
        when(elementVariable.getElement()).thenReturn(element);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        NodeVariable output = implementation.createNodeVariable(pageVariable, elementVariable);

        // assert
        Assert.assertEquals(output.getDriver(), driver);
        Assert.assertEquals(output.getElement(), element);
    }

    /**
     * Test the httpGet function.
     *
     * @throws MalformedURLException
     */
    @Test
    public void testHttpGet() throws MalformedURLException, InvalidUrlException {
        //mock
        WebServiceImpl implementation = spy(new WebServiceImpl());

        WebVariable pageVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);

        PhantomJSDriver jsdriver = mock(PhantomJSDriver.class);

        doReturn(jsdriver).when(implementation).getJSDriver(driver);

        // run
        implementation.httpGet(pageVariable, "http://url");

        // verify
        verify(jsdriver, times(2)).get(anyString());
    }

    @DataProvider(name = "validUrlValues")
    private Object[][] getValidUrls() {
        return new Object[][]{
                {"http://www.google.nl", null},
                {"https://www.google.nl", null}
        };
    }

    @DataProvider(name = "invalidUrlValues")
    private Object[][] getInvalidUrls() {
        return new Object[][]{
                {"ftp://www.google.nl", null},
                {null, null}
        };
    }

    /**
     * Test the createPageVariable function.
     */
    @Test
    public void testCreatePageVariable() {
        // mock
        WebDriver driver = mock(WebDriver.class);

        //The options variable
        nl.xillio.xill.plugins.web.data.Options options = mock(nl.xillio.xill.plugins.web.data.Options.class);
        when(options.createDriver()).thenReturn(driver);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        PageVariable pageVariable = implementation.createPage(options);

        // verify
        verify(options, times(1)).createDriver();

        //assert
        Assert.assertEquals(pageVariable.getDriver(), driver);
    }

    /**
     * Test the createPageVariable function when no options for the page are provided.
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testCreatePageVariableWithoutOptions() {

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.createPage(null);
    }

    /**
     * Test the setDriverOptions function
     */
    @Test
    public void testSetDriverOptionsWithTimeoutValue() {
        WebVariable pageVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);

        nl.xillio.xill.plugins.web.data.Options webOptions = mock(nl.xillio.xill.plugins.web.data.Options.class);
        when(webOptions.getTimeOut()).thenReturn(42);
        when(webOptions.getResolution()).thenReturn(new Dimension(1024,768));
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);
        Timeouts timeouts = mock(Timeouts.class);
        when(options.timeouts()).thenReturn(timeouts);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.setDriverOptions(pageVariable, webOptions);

        // verify
        verify(driver, times(2)).manage();
        verify(timeouts, times(1)).pageLoadTimeout(42, TimeUnit.MILLISECONDS);
    }

    /**
     * Test the setDriverOptions function
     */
    @Test
    public void testSetDriverOptionsWithInfiniteTimeoutValue() {
        WebVariable pageVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(pageVariable.getDriver()).thenReturn(driver);

        nl.xillio.xill.plugins.web.data.Options webOptions = mock(nl.xillio.xill.plugins.web.data.Options.class);
        when(webOptions.getTimeOut()).thenReturn(-1);
        when(webOptions.getResolution()).thenReturn(new Dimension(1024,768));
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);
        Timeouts timeouts = mock(Timeouts.class);
        when(options.timeouts()).thenReturn(timeouts);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.setDriverOptions(pageVariable, webOptions);

        // verify
        verify(driver, times(2)).manage();
        verify(timeouts, times(1)).pageLoadTimeout(-1, TimeUnit.MILLISECONDS);
    }

    /**
     * Test the quit function.
     */
    @Test
    public void testQuit() {
        // mock
        WebDriver driver = mock(WebDriver.class);
        WebVariable webVariable = mock(WebVariable.class);
        when(webVariable.getDriver()).thenReturn(driver);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.quit(webVariable);

        // verify
        verify(driver, times(1)).quit();
    }

    /**
     * Test the getPageFromPool function
     */
    @Test
    public void testGetPageFromPool() {
        // mock

        //the options
        nl.xillio.xill.plugins.web.data.Options options = mock(nl.xillio.xill.plugins.web.data.Options.class);
        PageVariable pageVariable = mock(PageVariable.class);
        Entity entity = mock(Entity.class);

        //the pool
        Identifier id = mock(Identifier.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);
        when(pool.createIdentifier(options)).thenReturn(id);
        when(pool.get(eq(id), any())).thenReturn(entity);
        when(entity.getPage()).thenReturn(pageVariable);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        WebVariable output = implementation.getEntityFromPool(pool, options).getPage();

        verify(pool, times(1)).createIdentifier(options);
        verify(pool, times(1)).get(eq(id), any());
        verify(entity, times(1)).getPage();

        Assert.assertEquals(output, pageVariable);
    }

    /**
     * Tests the getPageFromPool function when we have a full PhantomJSpool.
     */
    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testFullPhantomPool() {
        // mock
        nl.xillio.xill.plugins.web.data.Options options = mock(nl.xillio.xill.plugins.web.data.Options.class);
        PhantomJSPool pool = mock(PhantomJSPool.class);
        when(pool.createIdentifier(options)).thenReturn(null);
        when(pool.get(any(), any())).thenReturn(null);

        // run
        WebServiceImpl implementation = new WebServiceImpl();
        implementation.getEntityFromPool(pool, null);
    }

    /**
     * Test the download() method with normal usage
     */
    @Test
    public void testDownload() throws IOException {
        // mock
        CookieStore cookieStore = mock(BasicCookieStore.class);

        WebVariable webVariable = mockWebVariable();

        String url = "http://www.google.com";
        Path file = mock(Path.class);

        WebServiceImpl implementation = spy(new WebServiceImpl());
        doReturn(cookieStore).when(implementation).createCookieStore(any());
        doNothing().when(implementation).copyInputStreamToFile(any(), any());

        // run
        implementation.download(url, file, webVariable, 1000);

        // verify
        verify(implementation, times(1)).copyInputStreamToFile(any(), any());
    }

    /**
     * @return A web variable with a mocked {@link WebDriver}
     */
    private WebVariable mockWebVariable() {
        WebVariable webVariable = mock(WebVariable.class);
        WebDriver driver = mock(WebDriver.class);
        when(webVariable.getDriver()).thenReturn(driver);
        Options options = mock(Options.class);
        when(driver.manage()).thenReturn(options);
        when(options.getCookies()).thenReturn(null);
        return webVariable;
    }

    /**
     * Test the download() method when URL is invalid
     */
    @Test(expectedExceptions = MalformedURLException.class)
    public void testDownloadInvalidURL() throws IOException {
        // mock
        WebServiceImpl implementation = spy(new WebServiceImpl());

        String url = "www.google.com"; // invalid URL
        Path file = mock(Path.class);

        // run
        implementation.download(url, file, null, 1000);
    }

    /**
     * Test that an ssl context that trusts all certificates is created.
     */
    @Test
    public void testDownloadHttps() throws IOException, GeneralSecurityException {
        // mock
        WebVariable webVariable = mockWebVariable();
        WebServiceImpl implementation = spy(new WebServiceImpl());
        SSLContext context = mock(SSLContext.class);
        HttpClientBuilder clientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity entity = mock(HttpEntity.class);
        CookieStore cookieStore = mock(BasicCookieStore.class);

        doReturn(clientBuilder).when(implementation).createHttpClientBuilder();
        doReturn(context).when(implementation).createTrustAllSSLContext();
        doNothing().when(implementation).copyInputStreamToFile(any(), any());
        doReturn(httpClient).when(clientBuilder).build();
        doReturn(response).when(httpClient).execute(any());
        doReturn(entity).when(response).getEntity();
        doReturn(cookieStore).when(implementation).createCookieStore(any());
        doReturn(statusLine).when(response).getStatusLine();
        doReturn(200).when(statusLine).getStatusCode();

        String url = "https://google.com";

        // run
        int httpCode = implementation.download(url, Paths.get("42"), webVariable, 1000);

        // assert
        Assert.assertEquals(httpCode, 200);

        // verify
        verify(implementation).createTrustAllSSLContext();
    }

    /**
     * Test that the correct exception is thrown when an exception occurs during the creation of the SSL context.
     */
    @Test(expectedExceptions = IOException.class)
    public void testSSLContextFail() throws GeneralSecurityException, IOException {
        // mock
        WebServiceImpl implementation = spy(new WebServiceImpl());
        doThrow(GeneralSecurityException.class).when(implementation).createTrustAllSSLContext();

        String url = "https://google.com";

        // run
        implementation.download(url, Paths.get("42"), null, 1000);
    }
}
