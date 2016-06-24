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
package nl.xillio.xill.plugins.xurl.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.plugins.xurl.services.*;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import java.io.IOException;
import java.net.*;

/**
 * This class represents an abstract implementation of the request constructs.
 *
 * @author Thomas Biesaart
 */
abstract class AbstractRequestConstruct extends Construct {
    private final boolean allowBody;
    private BodyFactory bodyFactory;
    private ResponseParser responseParser;
    private OptionsFactory optionsFactory;
    private ExecutorFactory executorFactory;
    private ActivityLogger activityLogger;

    protected AbstractRequestConstruct() {
        allowBody = !getClass().isAnnotationPresent(NoBody.class);
    }

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        if (allowBody) {
            return new ConstructProcessor(
                    (url, body, options) -> process(url, body, options, context),
                    new Argument("url", ATOMIC),
                    new Argument("body", NULL),
                    new Argument("options", emptyObject(), OBJECT)
            );
        }

        return new ConstructProcessor(
                (url, options) -> process(url, null, options, context),
                new Argument("url", ATOMIC),
                new Argument("options", emptyObject(), OBJECT)
        );
    }

    private MetaExpression process(MetaExpression url, MetaExpression body, MetaExpression optionsExpression, ConstructContext context) {
        URI uri = getUri(url);
        Options options = optionsFactory.build(optionsExpression);
        Request request = buildRequest(uri);

        // Build the body
        if (body != null && !body.isNull()) {
            bodyFactory.applyBody(request, body, options, context);
        }

        // Apply request options
        Executor executor = executorFactory.buildExecutor(options);
        options.apply(executor);
        options.apply(request);

        // Do preemptive authentication.
        executor.authPreemptive(URIUtils.extractHost(uri));

        activityLogger.handle(request, options, context.getRootLogger());

        // Execute the request
        Response response = execute(executor, request);

        return fromValue(request, response, context, options);
    }

    /**
     * Parse a result value from a response.
     *
     * @param request  the request
     * @param response the response wrapper
     * @param context  the context for the construct call
     * @param options  the options
     * @return the result
     */
    MetaExpression fromValue(Request request, Response response, ConstructContext context, Options options) {
        try {
            HttpResponse httpResponse = response.returnResponse();
            activityLogger.handle(request, httpResponse, options, context.getRootLogger());
            return responseParser.build(response, httpResponse, options);
        } catch (IOException e) {
            throw new RobotRuntimeException("Could not parse response: " + e.getMessage(), e);
        }
    }

    /**
     * This method will execute the request using the executor and handle all known exceptions.
     *
     * @param executor the executor
     * @param request  the request
     * @return the response
     * @throws RobotRuntimeException if anything goes wrong
     */
    Response execute(Executor executor, Request request) {
        try {
            return executor.execute(request);
        } catch (ConnectTimeoutException e) {
            throw new RobotRuntimeException("The connection to " + e.getHost() + " timed out", e);
        } catch (ClientProtocolException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpException) {
                throw new RobotRuntimeException("Protocol Error: " + cause.getMessage(), e);
            }
            throw new RobotRuntimeException("Unsupported protocol", e);
        } catch (UnknownHostException e) {
            throw new RobotRuntimeException("No host found at " + e.getMessage(), e);
        } catch (HttpHostConnectException e) {
            throw new RobotRuntimeException("Could not connect to host " + e.getHost(), e);
        } catch (IOException e) {
            throw new RobotRuntimeException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse a MetaExpression to a uri.
     *
     * @param expression the expression
     * @return the URI
     * @throws RobotRuntimeException if the expression {@link MetaExpression#isNull()} or not a valid url
     */
    URI getUri(MetaExpression expression) {
        if (expression.isNull()) {
            throw new RobotRuntimeException("The passed url cannot be null, please provide a url");
        }

        try {
            // We parse through URL to do early url syntax validation
            return new URL(expression.getStringValue()).toURI();
        } catch (MalformedURLException e) {
            throw new RobotRuntimeException("The provided url is not valid: " + e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new RobotRuntimeException(expression + " is not a valid uri: " + e.getReason(), e);
        }
    }

    /**
     * Create a request base for this construct.
     *
     * @param uri the uri for which to build the request
     * @return the request
     */
    protected abstract Request buildRequest(URI uri);

    @Inject
    void setExecutorFactory(ExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
    }

    @Inject
    void setOptionsFactory(OptionsFactory optionsFactory) {
        this.optionsFactory = optionsFactory;
    }

    @Inject
    void setBodyFactory(BodyFactory bodyFactory) {
        this.bodyFactory = bodyFactory;
    }

    @Inject
    void setResponseParser(ResponseParser responseParser) {
        this.responseParser = responseParser;
    }

    @Inject
    void setActivityLogger(ActivityLogger activityLogger) {
        this.activityLogger = activityLogger;
    }
}
