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
package nl.xillio.xill.plugins.rest.data;

import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * Support class for processing request options
 */
public class Options {

    private int timeout = 5000;
    private HashMap<String, String> headers = new HashMap<>();
    private String proxyHost = "";
    private int proxyPort = 0;
    private String proxyUser = "";
    private String proxyPass = "";
    private String authUser = "";
    private String authPass = "";
    private boolean insecure = false;

    /**
     * @param optionsVar the map of options and their values for request operation
     */
    public Options(final MetaExpression optionsVar) {

        if (optionsVar == ExpressionBuilderHelper.NULL || optionsVar.isNull()) {
            // no option specified - so default is used
            return;
        }

        if (optionsVar.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException("Invalid options variable!");
        }

        @SuppressWarnings("unchecked")
        Map<String, MetaExpression> optionParameters = (Map<String, MetaExpression>) optionsVar.getValue();
        for (Map.Entry<String, MetaExpression> entry : optionParameters.entrySet()) {
            processOption(entry.getKey(), entry.getValue());
        }

        this.checkOptions();
    }

    private void processOption(final String option, final MetaExpression value) {
        switch (option) {

            case "timeout":
                this.timeout = value.getNumberValue().intValue();
                break;

            case "proxyhost":
                this.proxyHost = value.getStringValue();
                break;

            case "proxyport":
                this.proxyPort = value.getNumberValue().intValue();
                break;

            case "proxyuser":
                this.proxyUser = value.getStringValue();
                break;

            case "proxypass":
                this.proxyPass = value.getStringValue();
                break;

            case "user":
                this.authUser = value.getStringValue();
                break;

            case "pass":
                this.authPass = value.getStringValue();
                break;

            case "headers":
                this.processHeaders(value);
                break;

            case "insecure":
                this.insecure = value.getBooleanValue();
                break;

            default:
                throw new RobotRuntimeException(String.format("Option [%1$s] is invalid!", option));
        }
    }

    private void processHeaders(final MetaExpression headersVar) {
        if (headersVar.getType() != ExpressionDataType.OBJECT) {
            throw new RobotRuntimeException("Invalid headers content in options variable!");
        }

        this.headers.clear();

        @SuppressWarnings("unchecked")
        Map<String, MetaExpression> headersMap = (Map<String, MetaExpression>) headersVar.getValue();
        for (Map.Entry<String, MetaExpression> entry : headersMap.entrySet()) {
            this.headers.put(entry.getKey(), entry.getValue().getStringValue());
        }
    }

    private void checkOptions() {
        if (this.authUser.isEmpty() != this.authPass.isEmpty()) {
            throw new RobotRuntimeException("User and password for server authentication must be set both or none!");
        }

        if (this.proxyUser.isEmpty() != this.proxyPass.isEmpty()) {
            throw new RobotRuntimeException("User and password for proxy authentication must be set both or none!");
        }

        if ((this.proxyPort != 0) && (this.proxyHost.isEmpty())) {
            throw new RobotRuntimeException("Proxy port cannot be set without host!");
        }
    }

    /**
     * @return the request processing timeout
     */
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * whether this only accepts secure SSL connections or not.
     *
     * @return
     */
    public boolean isInsecure() {
        return this.insecure;
    }

    /**
     * Set a authentication method to executor
     *
     * @param executor request executor
     */
    public void setAuth(final Executor executor) {
        // Server authentication
        if (!this.authUser.isEmpty()) {
            executor.auth(this.authUser, this.authPass);
        }

        // Proxy settings
        HttpHost httpProxy;
        if (!this.proxyHost.isEmpty()) {
            if (this.proxyPort == 0) {
                httpProxy = new HttpHost(this.proxyHost);
            } else {
                httpProxy = new HttpHost(this.proxyHost, this.proxyPort);
            }
            if (!this.proxyUser.isEmpty()) {
                executor.auth(httpProxy, this.proxyUser, this.proxyPass);
            }
            executor.authPreemptiveProxy(httpProxy);
        }
    }

    /**
     * Add HTTP header items from "headers" option
     *
     * @param request Existing REST request
     */
    public void setHeaders(final Request request) {
        this.headers.forEach((k, v) -> request.addHeader(k, v));
    }

}
