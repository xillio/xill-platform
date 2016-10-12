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
package nl.xillio.xill.plugins.web.data;

import me.biesaart.utils.FileUtils;
import me.biesaart.utils.IOUtils;
import me.biesaart.utils.Log;
import nl.xillio.util.XillioHomeFolder;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.WebXillPlugin;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Class encapsulate PhantomJS options handling (parsing, validating, creating new PhantomJS process, etc.)
 * Class attributes represents all browser options for use in the loadpage function
 * It contains both CLI options and non-CLI options.
 * CLI options are those that must be provided when PhantomJS is starting and cannot be changed anymore for already started PhantomJS process
 * non-CLI options are those that can be set whenever at whatever existing PhantomJS process
 */
public class Options implements MetadataExpression {
    private static final File PHANTOM_JS_BIN = new File(XillioHomeFolder.forXill3(), "bin/web/phantomjs");
    private static final String PHANTOM_JS_LINUX_RESOURCE = "phantomjs/linux/phantomjs";
    private static final String PHANTOM_JS_WINDOWS_RESOURCE = "phantomjs/windows/phantomjs.exe";
    private static final String PHANTOM_JS_MAC_RESOURCE = "phantomjs/mac/phantomjs";
    private static final Logger LOGGER = Log.get();

    // Driver options
    private int timeout = 0;

    // DCap options
    private String browser;
    private boolean enableJS = true;
    private boolean enableWebSecurity = true;
    private boolean insecureSSL = false;
    private boolean loadImages = true;
    private String sslProtocol; // null==(default==) "sslv3"
    private boolean ltrUrlAccess; // --local-to-remote-url-access (default==null== "false")
    private Dimension resolution = new Dimension(1920, 1024); // Default viewport size

    private String proxyHost;
    private int proxyPort = 0;
    private String proxyUser;
    private String proxyPass;
    private String proxyType;
    private String httpAuthUser;
    private String httpAuthPass;

    /**
     * Set the proxy host.
     *
     * @param name The name of the host.
     */
    public void setProxyHost(final String name) {
        proxyHost = name;
    }

    /**
     * Set the proxy port.
     *
     * @param value The name of the port.
     */
    public void setProxyPort(final int value) {
        proxyPort = value;
    }

    /**
     * Set the timeout value.
     *
     * @param value The value of the timeout in ms.
     */
    public void setTimeout(final int value) {
        timeout = value;
    }

    /**
     * Set the proxy user.
     *
     * @param name The username.
     */
    public void setProxyUser(final String name) {
        proxyUser = name;
    }

    /**
     * Set the proxy pass.
     *
     * @param name The name of the pass.
     */
    public void setProxyPass(final String name) {
        proxyPass = name;
    }

    /**
     * Set proxy type.
     *
     * @param name The name of the proxyType. (Supported: http, socks5)
     */
    public void setProxyType(final String name) {
        proxyType = name;
    }

    /**
     * Set the httpAuthUser.
     *
     * @param name The name of the user.
     */
    public void setHttpAuthUser(final String name) {
        httpAuthUser = name;
    }

    /**
     * Set a pass for the httpAuthUser.
     *
     * @param name The name of the pass.
     */
    public void setHttpAuthPass(final String name) {
        httpAuthPass = name;
    }

    /**
     * Set the browser.
     *
     * @param name The name of the browser. (currently supported: PHANTOMJS)
     */
    public void setBrowser(final String name) {
        browser = name;
    }

    /**
     * Set the sslProtocol.
     *
     * @param name The name of the protocol. (supported: sslv3, sslv2, tlsv1, any).
     */
    public void setSslProtocol(final String name) {
        sslProtocol = name;
    }

    /**
     * Enable or disable JavaScript.
     *
     * @param enabled Whether or not we want JS enabled.
     */
    public void enableJS(final boolean enabled) {
        enableJS = enabled;
    }

    /**
     * Enable of disable WebSecurity.
     *
     * @param enabled Whether or not we want security enabled.
     */
    public void enableWebSecurity(final boolean enabled) {
        enableWebSecurity = enabled;
    }

    /**
     * Enable or disable insecure SSL.
     *
     * @param enabled Whether or not we want insecure SSL enabled.
     */
    public void enableInsecureSSL(final boolean enabled) {
        insecureSSL = enabled;
    }

    /**
     * Enable or disable load images.
     *
     * @param enabled Whether or not we want load images enabled.
     */
    public void enableLoadImages(final boolean enabled) {
        loadImages = enabled;
    }

    /**
     * Set the viewport dimension.
     *
     * @param width  The width of viewport in the pixels.
     * @param height The height of viewport in the pixels.
     */
    public void setResolution(int width, int height) {
        this.resolution = new Dimension(width, height);
    }

    /**
     * Enable or disable ltr URL access.
     *
     * @param enabled Whether or not we want this access enabled.
     */
    public void enableLtrUrlAccess(final boolean enabled) {
        ltrUrlAccess = enabled;
    }

    /**
     * @return current proxy user
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * @return current proxy pasword
     */
    public String getProxyPass() {
        return proxyPass;
    }

    /**
     * @return current HTTP auth user
     */
    public String getHttpAuthUser() {
        return httpAuthUser;
    }

    /**
     * @return current HTTP auth password
     */
    public String getHttpAuthPass() {
        return httpAuthPass;
    }

    /**
     * Creates new PhantomJS process - it uses current (CLI + non-CLI) options for starting the process
     *
     * @return newly created PhantomJS process
     */
    public WebDriver createDriver() {
        try {
            return createPhantomJSDriver();
        } catch (IllegalStateException e) {
            throw new RobotRuntimeException("Could not create a PhantomJS driver, please check your PhantomJS installation. See the Web.loadPage() function help page for installation details.", e);
        }
    }

    /**
     * It sets the non-CLI options (i.e. the option that can be set after the process is created)
     *
     * @param driver Existing WebDriver
     */
    public void setDriverOptions(final WebDriver driver) {
        // setting up the size of viewport
        driver.manage().window().setSize(resolution);

        // page load timeout
        if (timeout != 0) {
            driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
        } else {
            // set infinite timeout
            driver.manage().timeouts().pageLoadTimeout(-1, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Creates the object that holds all current CLI options.
     *
     * @return The object encapsulating all CLI parameters for PhantomJS process
     */
    private DesiredCapabilities createDCapOptions() {
        DesiredCapabilities dCap = DesiredCapabilities.phantomjs();

        // enable JavaScript
        dCap.setJavascriptEnabled(enableJS);

        ArrayList<String> phantomArgs = new ArrayList<>();
        phantomArgs.add("--disk-cache=false");
        // phantomArgs.add("--webdriver-logfile=NONE"); //! this option doesn't work (why not?) - it will create an empty file anyway
        phantomArgs.add("--webdriver-loglevel=NONE");// values can be NONE | ERROR | WARN | INFO | DEBUG (if NONE then the log file is not created)
        phantomArgs.add("--ignore-ssl-errors=" + Boolean.toString(insecureSSL));
        phantomArgs.add("--load-images=" + Boolean.toString(loadImages));
        phantomArgs.add("--web-security=" + Boolean.toString(enableWebSecurity));
        phantomArgs.add("--local-to-remote-url-access=" + Boolean.toString(ltrUrlAccess));

        if (sslProtocol != null) {
            phantomArgs.add("--ssl-protocol=" + sslProtocol);
        }

        if (proxyHost != null) {
            phantomArgs.add("--proxy-type=" + proxyType);
            phantomArgs.add(String.format("--proxy=%1$s:%2$d", proxyHost, proxyPort));
            if (proxyUser != null) {
                phantomArgs.add(String.format("--proxy-auth=%1$s:%2$s", proxyUser, proxyPass));
            }
        }

        if (httpAuthUser != null) {
            String s = String.format("%1$s:%2$s", httpAuthUser, httpAuthPass);
            dCap.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX, "Authorization: Basic " + Base64.encodeBase64String(s.getBytes()));
        }

        dCap.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);

        return dCap;
    }

    /**
     * @param s1 first string value
     * @param s2 second string value
     * @return if provided string are equal or not (including null strings)
     */
    private static boolean strEq(final String s1, final String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s2 != null && s1.equals(s2);
    }

    /**
     * It compares provided CLI options with current CLI options
     *
     * @param options contains actual LoadPage CLI settings
     * @return true if matches otherwise false
     */
    public boolean compareDCap(final Options options) {
        return strEq(dCapString(), options.dCapString());
    }

    private String dCapString() {
        return StringUtils.join(
                browser, enableJS, enableWebSecurity,
                insecureSSL, loadImages, sslProtocol,
                ltrUrlAccess, proxyHost, proxyPort,
                proxyUser, proxyPass, proxyType,
                httpAuthUser, httpAuthPass, resolution);
    }

    private WebDriver createPhantomJSDriver() {
        // creates CLI options
        DesiredCapabilities dcap = createDCapOptions();

        // creates new PhantomJS process with given CLI options
        PhantomJSDriver driver = new PhantomJSDriver(dcap);

        // set other (non-CLI) options
        setDriverOptions(driver);

        return driver;
    }

    /**
     * Creates new PhantomJS.exe file in temporary folder - on MS Windows only
     * For other operating systems, PhantomJS is expected to be properly installed in the path.
     */
    public static void extractNativeBinary() {

        try {
            System.setProperty("phantomjs.binary.path", PHANTOM_JS_BIN.getAbsolutePath());

            if (PHANTOM_JS_BIN.exists()) {
                // We are done here
                return;
            }

            LOGGER.info("Deploying PhantomJS binary");

            // extract file into the current directory
            String phantomJsResourcePath = getOsSpecificPhantomJSResourcePath();
            FileUtils.touch(PHANTOM_JS_BIN);

            try (InputStream reader = WebXillPlugin.class.getResourceAsStream(phantomJsResourcePath); FileOutputStream writer = new FileOutputStream(PHANTOM_JS_BIN)) {
                IOUtils.copy(reader, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to deploy PhantomJS binary", e);
        }
    }

    private static String getOsSpecificPhantomJSResourcePath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return PHANTOM_JS_WINDOWS_RESOURCE;
        }

        if (SystemUtils.IS_OS_MAC) {
            return PHANTOM_JS_MAC_RESOURCE;
        }

        if (SystemUtils.IS_OS_UNIX) {
            return PHANTOM_JS_LINUX_RESOURCE;
        }

        throw new UnsupportedOperationException("PhantomJS is not supported on " + SystemUtils.OS_NAME);
    }

    /**
     * @return Returns the timeOut value in the options.
     */
    public int getTimeOut() {
        return timeout;
    }

    /**
     * @return Returns the resolution value in the options.
     */
    public Dimension getResolution() {
        return resolution;
    }

}
