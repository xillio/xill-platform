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

import com.google.inject.ImplementedBy;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.web.InvalidUrlException;
import nl.xillio.xill.plugins.web.data.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.StaleElementReferenceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Provides an interface for a webService.
 *
 * @author Ivor van der hoog
 * @author Thomas Biesaart
 */
@ImplementedBy(WebServiceImpl.class)
public interface WebService {

    /**
     * Tries to click a {@link WebVariable}.
     *
     * @param var The variable we want to click.
     * @throws StaleElementReferenceException Throws an exception when the element is stale.
     */
    void click(WebVariable var);

    /**
     * Sets focus on an element on a given page.
     *
     * @param var The element we want to move to.
     */
    void moveToElement(WebVariable var);

    /**
     * Returns the tag name of a {@link WebVariable}.
     *
     * @param var The variable we want the tag of.
     * @return The name of the tag of the variable.
     */
    String getTagName(WebVariable var);

    /**
     * Returns a given attribute of a {@link WebVariable}.
     *
     * @param var  The variable we want to get an attribute from
     * @param name The name of the attribute we want to retrieve.
     * @return The attribute of the element.
     */
    String getAttribute(WebVariable var, String name);

    /**
     * Returns the text contained in a {@link WebVariable}.
     *
     * @param var The variable we want to get text from.
     * @return Returns the text in the element.
     */
    String getText(WebVariable var);


    /**
     * Returns the HTML content of the web page.
     *
     * @param page The web page.
     * @return HTML content of the web page.
     */
    String getSource(final PageVariable page);

    /**
     * Returns the title of a {@link WebVariable}.
     *
     * @param var The variable we want to a title from.
     * @return The title.
     */
    String getTitle(WebVariable var);

    /**
     * Finds all the elements in a node given a cssPath.
     *
     * @param var     The {@link WebVariable} we're searching.
     * @param cssPath The cssPath we're using.
     * @return Returns a list of elements: {@link WebVariable}.
     * @throws OperationFailedException if the cssPath query is invalid
     */
    List<WebVariable> findElementsWithCssPath(WebVariable var, String cssPath);

    /**
     * Finds all the elements in a node given a cssPath.
     *
     * @param var   The {@link WebVariable} we're searching.
     * @param xpath The xpath we're using.
     * @return Returns a list of elements: {@link WebVariable}.
     * @throws OperationFailedException if the xpath query is invalid
     */
    List<WebVariable> findElementsWithXpath(WebVariable var, String xpath);

    /**
     * Returns the current URL of a {@link WebVariable}.
     *
     * @param var the variable we want the url from.
     * @return A string which is the current URL.
     */
    String getCurrentUrl(WebVariable var);

    /**
     * Clears a {@link WebVariable}.
     *
     * @param var the variable we want to clear.
     */
    void clear(WebVariable var);

    /**
     * Simulates sending a key to an {@link WebVariable}.
     * May alter its value.
     *
     * @param var The variable we want to send keys to.
     * @param key the key we want to send.
     */
    void sendKeys(WebVariable var, String key);

    /**
     * Gets the cookies from a {@link WebVariable}.
     *
     * @param var The webpage we're retrieving cookies from.
     * @return A set of Cookies.
     */
    Set<Cookie> getCookies(WebVariable var);

    /**
     * Gets the name of a {@link Cookie}.
     *
     * @param cookie The cookie.
     * @return The name of the cookie.
     */
    String getName(Cookie cookie);

    /**
     * Gets the domain of a {@link Cookie}.
     *
     * @param cookie The cookie.
     * @return The domain of the cookie.
     */
    String getDomain(Cookie cookie);

    /**
     * Gets the path of a {@link Cookie}.
     *
     * @param cookie The cookie.
     * @return The path of the cookie.
     */
    String getPath(Cookie cookie);

    /**
     * Gets the value of a {@link Cookie}.
     *
     * @param cookie The cookie.
     * @return The value of the cookie.
     */
    String getValue(Cookie cookie);

    /**
     * Deletes the cookie in the {@link WebVariable} with the given name.
     *
     * @param var  The variable we want to delete cookies on.
     * @param name The name of the cookie we're deleting.
     */
    void deleteCookieNamed(WebVariable var, String name);

    /**
     * Deletes all cookies on a {@link WebVariable}.
     *
     * @param var The variable we're deleting cookies on.
     */
    void deleteCookies(WebVariable var);

    /**
     * Makes a screenshot on a {@link WebVariable} and returns it as file.
     *
     * @param var The variable we're screenshotting.
     * @return A path to the file containing the screenshot.
     */
    Path getScreenshotAsFilePath(WebVariable var);

    /**
     * Checks whether a {@link WebVariable} is selected.
     *
     * @param var The variable we want to query.
     * @return Returns whether the variable is selected.
     */
    boolean isSelected(WebVariable var);

    /**
     * Switches to a given frame.
     *
     * @param page    The page we want to switch to.
     * @param element The element we want to switch to.
     */
    void switchToFrame(WebVariable page, WebVariable element);

    /**
     * Switches to a given frame.
     *
     * @param var     the page we want to switch to.
     * @param element The element we want to switch to.
     */
    void switchToFrame(WebVariable var, String element);

    /**
     * Switches to a given frame.
     *
     * @param driver  The driver we're using.
     * @param element The frame as an integer.
     */
    void switchToFrame(WebVariable driver, int element);

    /**
     * Places a Cookie onto a WebVariable.
     *
     * @param var    The webVariable we want to place the cookie on
     * @param cookie The cookie
     */
    void addCookie(WebVariable var, CookieVariable cookie);

    /**
     * Creates a node variable with a given page and element.
     *
     * @param page    The page of the node.
     * @param element The element of the node.
     * @return A new node.
     */
    NodeVariable createNodeVariable(WebVariable page, WebVariable element);

    /**
     * Executes HTTP GET on a {@link WebVariable} and an URL.
     *
     * @param var The webpage.
     * @param url The url.
     * @throws ClassCastException    The implementations casts to a class. When that goes wrong we need to catch it.
     * @throws MalformedURLException When the url is malformed an exception can occur.
     * @throws InvalidUrlException   When the url is invalid an exception can occur.
     */
    void httpGet(WebVariable var, String url) throws MalformedURLException, InvalidUrlException;

    /**
     * Creates a page as a {@link WebVariable} given a {@link Options} as setting.
     *
     * @param options The options.
     * @return The page
     * @throws NullPointerException When the options are null.
     */
    PageVariable createPage(Options options);

    /**
     * Drags a PhantomJS entity from the pool.
     * Creates it when it doesn't exist yet.
     *
     * @param pool    The pool we're extracting the page from.
     * @param options The options the page has.
     * @return The entity.
     */
    PhantomJSPool.Entity getEntityFromPool(PhantomJSPool pool, Options options);

    /**
     * Sets the drivers default options of a {@link WebVariable}.
     *
     * @param var     The variable we're setting the options for.
     * @param timeOut A timeout in milliseconds.
     */
    void setDriverOptions(WebVariable var, int timeOut);

    /**
     * Closes a {@link WebVariable} and all pages associated.
     *
     * @param var The variable we want to quit.
     */
    void quit(WebVariable var);

    /**
     * Download a content given by URL link and save it to a file
     *
     * @param url            The URL of the link that has to be downloaded
     * @param targetFilePath A file where the downloaded content is to be stored
     * @param context        An optional {@link WebVariable} that is used for take-over the cookies and use is during the download
     * @param timeout        The timeout in miliseconds
     * @return HTTP response code
     * @throws IOException When download fails - for more reasons
     */
    int download(String url, Path targetFilePath, WebVariable context, int timeout) throws IOException;
}
