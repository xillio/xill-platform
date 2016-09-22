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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.web.data.WebVariable;
import nl.xillio.xill.plugins.web.services.web.WebService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * Download a content given by URL link and save it to a file
 */
public class DownloadConstruct extends PhantomJSConstruct {
    private static final int DEFAULT_TIMEOUT = 5000;

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                (url, fileName, webContext, timeout) -> process(url, fileName, webContext, timeout, context),
                new Argument("url", ATOMIC),
                new Argument("filename", ATOMIC),
                new Argument("webContext", NULL, ATOMIC),
                new Argument("timeout", fromValue(DEFAULT_TIMEOUT), ATOMIC));
    }

    /**
     * @param urlVar        The URL of the link that has to be downloaded
     * @param fileName      A file where the downloaded content is to be stored
     * @param webContextVar An optional page or node variable that is used for take-over the cookies and use is during the download
     * @param timeoutVar    The timeout in miliseconds
     * @param context       The context of this construct
     * @throws OperationFailedException target could not be downloaded
     * @return null variable
     */
    private MetaExpression process(final MetaExpression urlVar, final MetaExpression fileName, final MetaExpression webContextVar, final MetaExpression timeoutVar,final ConstructContext context) {

        String url = urlVar.getStringValue();
        if (url.isEmpty()) {
            throw new RobotRuntimeException("Invalid variable value. URL is empty.");
        }
        if (fileName.getStringValue().isEmpty()) {
            throw new RobotRuntimeException("Invalid variable value. Filename is empty.");
        }
        Path targetFile = getPath(context, fileName);

        WebVariable webContext = null;
        if (!webContextVar.isNull()) {
            if (isNodeAndNotPage(webContextVar)) {
                webContext = getNode(webContextVar);
            } else {
                webContext = getPage(webContextVar);
            }
        }

        int timeout = DEFAULT_TIMEOUT; // Default timeout (in miliseconds)
        if (!timeoutVar.isNull()) {
            timeout = timeoutVar.getNumberValue().intValue();
        }

        try {
            return fromValue(getWebService().download(url, targetFile, webContext, timeout));
        } catch (MalformedURLException e) {
            throw new OperationFailedException("download from current URL.", "Invalid URL found.", e);
        } catch (IOException e) {
            throw new OperationFailedException("write result to target", "IOException occurred.", e);
        }

    }
}
