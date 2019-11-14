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
package nl.xillio.xill.plugins.httpserver;

import com.sun.net.httpserver.HttpExchange;
import nl.xillio.xill.api.data.MetadataExpression;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Request implements MetadataExpression {
    private final HttpExchange exchange;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public void sendResponse(int statusCode, Map<String, Object> headers, @Nullable InputStream body) throws IOException {
        headers.forEach((key, value) -> exchange.getResponseHeaders().add(key, String.valueOf(value)));
        if (body == null) {
            exchange.sendResponseHeaders(statusCode, 0);
        } else {
            byte[] data = IOUtils.toByteArray(body);
            exchange.sendResponseHeaders(statusCode, data.length);
            exchange.getResponseBody().write(data);
        }
        exchange.getResponseBody().close();
        exchange.close();
    }

    public String getMethod() {
        return exchange.getRequestMethod();
    }

    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    public String getQuery() {
        return exchange.getRequestURI().getQuery();
    }

    public Map<String, List<String>> getHeaders() {
        return exchange.getRequestHeaders();
    }

    public InputStream getBody() {
        return exchange.getRequestBody();
    }
}
