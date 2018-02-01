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
package nl.xillio.xill.plugins.xurl.data;

import org.apache.http.Header;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.util.Optional;

import static nl.xillio.xill.plugins.xurl.services.OptionsFactory.parseContentType;

/**
 * This class represents a wrapper around all the http options for the XURL plugin.
 *
 * @author Thomas Biesaart
 */
public class Options {
    // Five minutes
    private static final int DEFAULT_TIMEOUT = 300000;

    private Credentials basicAuth;
    private ProxyOptions proxyOptions;
    private NTLMOptions ntlmOptions;
    private boolean insecure;
    private boolean multipart = false;
    private ContentType responseContentType;
    private int timeout = DEFAULT_TIMEOUT;
    private Header[] headers = new Header[0];
    private String logging;
    private boolean enableRedirect = true;
    private boolean ignoreConnectionCache = false;

    /**
     * Default constructor.
     */
    public Options() {
    }

    /**
     * Copy constructor.
     *
     * @param original otpins to copy
     */
    public Options(Options original) {
        this.basicAuth = original.basicAuth;
        this.proxyOptions = original.proxyOptions;
        this.ntlmOptions = original.ntlmOptions;
        this.insecure = original.insecure;
        this.multipart = original.multipart;
        this.responseContentType = original.responseContentType;
        this.timeout = original.timeout;
        this.headers = original.headers;
        this.logging = original.logging;
        this.enableRedirect = original.enableRedirect;
        this.ignoreConnectionCache = original.ignoreConnectionCache;
    }

    public Credentials getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(Credentials basicAuth) {
        this.basicAuth = basicAuth;
    }

    public boolean isBasicAuthEnabled() {
        return basicAuth != null;
    }

    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    public void setProxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
    }

    public boolean isProxyEnabled() {
        return proxyOptions != null;
    }

    public NTLMOptions getNTLMOptions() {
        return ntlmOptions;
    }

    public void setNTLMOptions(NTLMOptions ntlmOptions) {
        this.ntlmOptions = ntlmOptions;
    }

    public boolean isNTLMEnabled() {
        return ntlmOptions != null;
    }

    public boolean isInsecure() {
        return insecure;
    }

    public void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
    }

    public ContentType getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(ContentType responseContentType) {
        this.responseContentType = responseContentType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public String getLogging() {
        return logging;
    }

    public void setLogging(String logging) {
        this.logging = logging;
    }

    public boolean isEnableRedirect() {
        return enableRedirect;
    }

    public void setEnableRedirect(boolean enableRedirect) {
        this.enableRedirect = enableRedirect;
    }

    public boolean isIgnoreConnectionCache() {
        return ignoreConnectionCache;
    }

    public void setIgnoreConnectionCache(boolean ignoreConnectionCache) {
        this.ignoreConnectionCache = ignoreConnectionCache;
    }

    public Optional<ContentType> getBodyContentType() {
        for (Header header : headers) {
            if ("Content-Type".equals(header.getName())) {
                return Optional.of(parseContentType(header.getValue()));
            }
        }
        return Optional.empty();
    }

    public void apply(Request request) {
        if (timeout != 0) {
            request.connectTimeout(timeout);
            request.socketTimeout(timeout);
        }

        for (Header header : headers) {
            request.addHeader(header);
        }
        if (proxyOptions != null) {
            request.viaProxy(proxyOptions.getHttpHost());
        }
    }

    public void apply(Executor executor) {
        if (basicAuth != null) {
            executor.auth(basicAuth.getUsername(), basicAuth.getPassword());
        }

        if (ntlmOptions != null) {
            executor.auth(ntlmOptions.getUsername(), ntlmOptions.getPassword(), getNTLMOptions().getWorkstation(), ntlmOptions.getDomain());
        }

        if (proxyOptions != null) {
            if (proxyOptions.getCredentials() != null) {
                executor.auth(proxyOptions.getHttpHost(), proxyOptions.getUsername(), proxyOptions.getPassword());
            }
            executor.authPreemptiveProxy(proxyOptions.getHttpHost());
        }
    }
}
