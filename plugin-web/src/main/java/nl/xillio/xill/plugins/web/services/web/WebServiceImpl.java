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

import com.google.inject.Singleton;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.InvalidUrlException;
import nl.xillio.xill.plugins.web.data.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContexts;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The implementation of the {@link WebService} interface.
 */
@Singleton
public class WebServiceImpl implements WebService {

    @Override
    public void click(final WebVariable node) {
        try {
            WebElement webElement = node.getElement();
            webElement.click();
        } catch (StaleElementReferenceException e) {
            throw new OperationFailedException("click web element", "Stale element clicked.", e);
        }
    }

    @Override
    public void moveToElement(final WebVariable var) {
        try {
            WebDriver page = var.getDriver();
            WebElement element = var.getElement();
            new Actions(page).moveToElement(element).perform();
        } catch (RobotRuntimeException e1) {
            throw e1;
        } catch (Exception e2) {
            throw new OperationFailedException("move to web element.", "Unknown error occurred in Selenium: " + e2.getMessage(), e2);
        }
    }

    @Override
    public String getTagName(final WebVariable var) {
        WebElement element = var.getElement();
        return element.getTagName();
    }

    @Override
    public String getAttribute(final WebVariable var, final String name) {
        try {
            WebElement element = var.getElement();
            return element.getAttribute(name);
        } catch (Exception e) {
            throw new OperationFailedException("get attribute: " + name + " from Web variable", "Unknown Selenium error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getText(final WebVariable var) {
        String text;
        if (var instanceof PageVariable) {
            try {
                WebElement element = var.getDriver().findElement(By.xpath("//body"));
                text = element.getText();
            } catch (NoSuchElementException e) {
                throw new OperationFailedException("Extract text from Page Variable", "Cannot find <body> tag.", e);
            }
        } else {
            text = var.getElement().getText();
        }
        return text;
    }

    @Override
    public String getSource(final PageVariable page) {
        return page.getDriver().getPageSource();
    }

    @Override
    public List<WebVariable> findElementsWithCssPath(final WebVariable var, final String cssPath) {
        SearchContext node = var instanceof PageVariable ? var.getDriver() : var.getElement();
        List<WebElement> searchResults;
        try {
            searchResults = node.findElements(By.cssSelector(cssPath));
        } catch (InvalidElementStateException | InvalidSelectorException e) {
            throw new OperationFailedException("selecting the Web element.", "Invalid CSSPath found", e);
        }
        return searchResults.stream()
                .map(element -> new NodeVariable(null, element))
                .collect(Collectors.toList());
    }

    @Override
    public List<WebVariable> findElementsWithXpath(final WebVariable var, final String xpath) {
        SearchContext node = var instanceof PageVariable ? var.getDriver() : var.getElement();
        List<WebElement> searchResults;

        try {
            searchResults = node.findElements(By.xpath(xpath));
        } catch (InvalidSelectorException e) {
            throw new OperationFailedException("find element using XPath", "Invalid selector found", e);
        }

        return searchResults.stream()
                .map(element -> new NodeVariable(null, element))
                .collect(Collectors.toList());
    }

    @Override
    public String getCurrentUrl(final WebVariable var) {
        WebDriver driver = var.getDriver();
        return driver.getCurrentUrl();
    }

    @Override
    public void clear(final WebVariable var) {
        WebElement element = var.getElement();
        element.clear();
    }

    @Override
    public void sendKeys(final WebVariable var, final String key) {
        WebElement element = var.getElement();
        element.sendKeys(key);
    }

    @Override
    public String getTitle(final WebVariable var) {
        WebDriver driver = var.getDriver();
        return driver.getTitle();
    }

    @Override
    public Set<Cookie> getCookies(final WebVariable var) {
        WebDriver driver = var.getDriver();
        return driver.manage().getCookies();
    }

    @Override
    public String getName(final Cookie cookie) {
        return cookie.getName();
    }

    @Override
    public String getDomain(final Cookie cookie) {
        return cookie.getDomain();
    }

    @Override
    public String getPath(final Cookie cookie) {
        return cookie.getPath();
    }

    @Override
    public String getValue(final Cookie cookie) {
        return cookie.getValue();
    }

    @Override
    public void deleteCookieNamed(final WebVariable var, final String name) {
        WebDriver driver = var.getDriver();
        driver.manage().deleteCookieNamed(name);
    }

    @Override
    public void deleteCookies(final WebVariable var) {
        WebDriver driver = var.getDriver();
        driver.manage().deleteAllCookies();
    }

    @Override
    public Path getScreenshotAsFilePath(final WebVariable var, int width, int height) {
        WebDriver driver = var.getDriver();

        // Process the resolution
        Dimension orgResolution = null;
        if (width != 0 && height != 0) {
            orgResolution = driver.manage().window().getSize();
            if (orgResolution.getWidth() != width || orgResolution.getHeight() != height) {
                // Current resolution is different than required
                driver.manage().window().setSize(new Dimension(width, height)); // Set new resolution
            } else {
                // Current resolution is the same as required - so no change needed
                orgResolution = null; // Set this to null to let know that there will be no switching back
            }
        }

        // Do the screenshot
        PhantomJSDriver castedDriver = getJSDriver(driver);
        Path path = castedDriver.getScreenshotAs(OutputType.FILE).toPath();

        // Optionally switch resolution back
        if (orgResolution != null) {
            driver.manage().window().setSize(orgResolution); // Switch the viewport resolution back to the original (before this construct call) size
        }

        return path;
    }

    PhantomJSDriver getJSDriver(WebDriver driver) {
        return (PhantomJSDriver) driver;
    }

    @Override
    public boolean isSelected(final WebVariable var) {
        try {
            WebElement element = var.getElement();
            return element.isSelected();
        } catch (UnsupportedOperationException e1) {
            throw new OperationFailedException("select element.", "Operation not supported by selenium.", e1);
        } catch (WebDriverException e2) {
            throw new OperationFailedException("access webDriver when trying to select element", "Unknown selenium error.", e2);
        }
    }

    @Override
    public void switchToFrame(final WebVariable page, final WebVariable elem) {
        WebElement element = elem.getElement();
        WebDriver driver = page.getDriver();
        try {
            driver.switchTo().frame(element);
        } catch (NoSuchFrameException e) {
            throw new OperationFailedException("switch to frame.", "Requested frame doesn't exist.", e);
        }
    }

    @Override
    public void switchToFrame(final WebVariable var, final String element) {
        WebDriver driver = var.getDriver();
        try {
            driver.switchTo().frame(element);
        } catch (NoSuchFrameException e) {
            throw new OperationFailedException("switch to frame.", "Requested frame doesn't exist.", e);
        }
    }

    @Override
    public void switchToFrame(final WebVariable var, final int element) {
        WebDriver driver = var.getDriver();
        try {
            driver.switchTo().frame(element);
        } catch (NoSuchFrameException e) {
            throw new OperationFailedException("switch to frame", "No such frame exception encountered.", e);
        }
    }

    @Override
    public void addCookie(final WebVariable var, final CookieVariable cookieVar) {
        WebDriver driver = var.getDriver();
        Cookie cookie = new Cookie(cookieVar.getName(), cookieVar.getValue(), cookieVar.getDomain(), cookieVar.getPath(), cookieVar.getExpireDate(), false);
        driver.manage().addCookie(cookie);
    }

    @Override
    public NodeVariable createNodeVariable(final WebVariable page, final WebVariable element) {
        WebDriver newDriver = page.getDriver();
        WebElement newElement = element.getElement();
        return new NodeVariable(newDriver, newElement);
    }

    @Override
    public void httpGet(final WebVariable var, final String url) throws MalformedURLException, InvalidUrlException {
        // Check if the URL is supported.
        if (!checkSupportedURL(new URL(url))) {
            throw new MalformedURLException();
        }

        PhantomJSDriver driver = getJSDriver(var.getDriver());
        driver.get("about:blank");
        driver.get(url);

        // Check if the new page is blank while that is not requested.
        String newUrl = driver.getCurrentUrl();
        if ("about:blank".equals(newUrl) && !"about:blank".equals(url)) {
            throw new InvalidUrlException("The URL " + newUrl + " could not be found or is invalid.");
        }
    }

    boolean checkSupportedURL(URL url) {
        return "http".equalsIgnoreCase(url.getProtocol())
                || "https".equalsIgnoreCase(url.getProtocol())
                || "file".equalsIgnoreCase(url.getProtocol());
    }

    @Override
    public PageVariable createPage(final Options options) {
        if (options == null) {
            throw new NullPointerException("Options cannot be null.");
        }
        WebDriver driver = options.createDriver();
        return new PageVariable(driver, null);
    }

    @Override
    public void setDriverOptions(final WebVariable var, final Options options) {
        WebDriver driver = var.getDriver();
        // setting up the size of viewport
        driver.manage().window().setSize(options.getResolution());

        // page load timeout
        if (options.getTimeOut() != 0) {
            driver.manage().timeouts().pageLoadTimeout(options.getTimeOut(), TimeUnit.MILLISECONDS);
        } else {
            // set infinite timeout
            driver.manage().timeouts().pageLoadTimeout(-1, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void quit(final WebVariable var) {
        WebDriver driver = var.getDriver();
        driver.quit();
    }

    @Override
    public PhantomJSPool.Entity getEntityFromPool(final PhantomJSPool pool, final Options options) {
        PhantomJSPool.Entity pjsInstance = pool.get(pool.createIdentifier(options), this);
        if (pjsInstance == null) {
            throw new OperationFailedException("get PhantomJS Entity.", "PhantomJS pool is fully used and cannot provide another instance!");
        }
        return pjsInstance;
    }

    CookieStore createCookieStore(Set<Cookie> seleniumCookieSet) {
        CookieStore cookieStore = new BasicCookieStore();

        for (Cookie seleniumCookie : seleniumCookieSet) {
            BasicClientCookie cookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            cookie.setDomain(seleniumCookie.getDomain());
            cookie.setPath(seleniumCookie.getPath());
            cookie.setExpiryDate(seleniumCookie.getExpiry());
            cookie.setSecure(seleniumCookie.isSecure());
            cookieStore.addCookie(cookie);
        }
        return cookieStore;
    }

    void copyInputStreamToFile(final InputStream stream, final Path targetFilePath) throws IOException {
        // Make sure the target directory exists.
        try {
            Files.createDirectories(targetFilePath.getParent());
        } catch (IOException e) {
            throw new OperationFailedException("copy file to target location", "Was unable to create new folder in path: " + targetFilePath.getParent().toAbsolutePath() + ".", "Check if the supplied path is correct.");
        }
        Files.copy(stream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Create an SSL context that accepts all certificates
     *
     * @return A custom SSL context
     * @throws GeneralSecurityException When building an SSL context fails
     */
    SSLContext createTrustAllSSLContext() throws GeneralSecurityException {
        return SSLContexts.custom().loadTrustMaterial((certChain, authType) -> true).build();
    }

    /**
     * Create a custom {@link HttpClientBuilder}.
     *
     * @return A new {@link HttpClientBuilder} from {@link HttpClients}
     */
    HttpClientBuilder createHttpClientBuilder() {
        return HttpClients.custom();
    }

    @Override
    public int download(final String url, final Path targetFilePath, final WebVariable webContext, int timeout) throws IOException {

        // Check URL
        URL newURL = new URL(url);
        if (!checkSupportedURL(newURL)) {
            throw new MalformedURLException();
        }

        // Set timeout
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout).build();

        HttpClientBuilder builder = createHttpClientBuilder().setDefaultRequestConfig(requestConfig).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

        if (webContext != null) {
            // Takeover cookies from web context
            CookieStore cookieStore = createCookieStore(webContext.getDriver().manage().getCookies());
            HttpClientContext context = HttpClientContext.create();
            context.setCookieStore(cookieStore);

            // Create httpclient
            builder = createHttpClientBuilder().setDefaultCookieStore(cookieStore);
        }

        // Create an SSL context that does _not_ check the identity of the target: the trust mechanism is disabled
        if ("https".equalsIgnoreCase(newURL.getProtocol())) {
            SSLContext sslCtx;
            try {
                sslCtx = createTrustAllSSLContext();
            } catch (GeneralSecurityException e) {
                // cannot do anything about these exceptions
                throw new IOException("Cannot create the SSL context for " + url, e);
            }

            //And attach to the request
            builder.setSSLContext(sslCtx);
        }

        HttpGet httpget = new HttpGet(url);

        try (CloseableHttpResponse response = builder.build().execute(httpget)) {
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                try (InputStream stream = entity.getContent()) {
                    copyInputStreamToFile(stream, targetFilePath);
                } catch (NoSuchFileException e) {
                    throw new OperationFailedException("copy input to file", "Could not create the file " + targetFilePath.toFile().getAbsolutePath() + "\nCheck if the provided path is valid.", e);
                }
            } else {
                throw new OperationFailedException("process httpRequest", "No httpEntity found.");
            }

            return response.getStatusLine().getStatusCode();
        }
    }
}
