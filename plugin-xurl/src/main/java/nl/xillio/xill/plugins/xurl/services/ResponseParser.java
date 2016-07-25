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
package nl.xillio.xill.plugins.xurl.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.data.XmlNodeFactory;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.TRUE;
import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * This class is responsible for building MetaExpressions from Responses.
 *
 * @author Thomas Biesaart
 */
@Singleton
public class ResponseParser {
    private static final String HEADER_COOKIES = "Set-Cookie";
    private final JsonParser jsonParser;
    private final XmlNodeFactory xmlNodeFactory;

    @Inject
    public ResponseParser(JsonParser jsonParser, XmlNodeFactory xmlNodeFactory) {
        this.jsonParser = jsonParser;
        this.xmlNodeFactory = xmlNodeFactory;
    }

    /**
     * Build a MetaExpression representation of this response.
     * This representation will contain 4 fields:
     * - status
     * - headers
     * - version
     * - body
     *
     * @param response     the response
     * @param httpResponse the response content
     * @param options      the options
     *                     the value of the Content-Type header will be used
     * @return the expression
     * @throws IOException if an IO error occurs
     */
    public MetaExpression build(Response response, HttpResponse httpResponse, Options options) throws IOException {

        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>(3);

        MetaExpression status = parseStatus(httpResponse.getStatusLine());
        result.put("status", status);
        result.put("headers", parseHeaders(httpResponse.getAllHeaders()));
        result.put("version", parseVersion(httpResponse.getProtocolVersion()));
        result.put("cookies", parseCookies(httpResponse.getHeaders(HEADER_COOKIES)));
        boolean preventDiscard = false;

        if (httpResponse.getEntity() != null) {
            MetaExpression body = parseBody(httpResponse, options.getResponseContentType(), status);
            result.put("body", body);

            // Prevent discard if the body is a stream
            preventDiscard = body.getBinaryValue().hasInputStream();
        }

        if (!preventDiscard) {
            response.discardContent();
        }

        return fromValue(result);
    }

    /**
     * Parse the body of the response.
     * The content will be parsed by the value of the content type header.
     * If the content type contains json it will be parsed from json.
     *
     * @param httpResponse        the response
     * @param contentTypeOverride the content type that should be read
     * @return the result
     * @throws IOException if an io error occurs
     */
    MetaExpression parseBody(HttpResponse httpResponse, ContentType contentTypeOverride, MetaExpression status) throws IOException {
        String contentType = getContentType(httpResponse, contentTypeOverride);
        InputStream inputStream = httpResponse.getEntity().getContent();

        if (contentType.contains("json")) {
            return bodyAsJson(inputStream, status);
        }

        if (contentType.contains("xml")) {
            return bodyAsXML(inputStream, status);
        }

        if (contentType.contains("text")) {
            return bodyAsText(inputStream);
        }

        // As a final fallback we return the stream itself
        return fromValue(new SimpleIOStream(inputStream, contentType));
    }

    private String getContentType(HttpResponse response, ContentType override) {
        if (override != null) {
            return override.toString();
        }
        return response.getEntity().getContentType().getValue();
    }

    private MetaExpression bodyAsJson(InputStream inputStream, MetaExpression status) throws IOException {
        try {
            String content = IOUtils.toString(inputStream);
            Object value = jsonParser.fromJson(content, Object.class);
            return MetaExpression.parseObject(value);
        } catch (JsonException e1) {
            throw throwBodyParseError(e1, "JSON", status);
        } catch (IllegalArgumentException e2) {
            throw new OperationFailedException("ParseResponseBodyAsJSON", e2.getMessage(), "Illegal arguments were handed down.", e2);
        }
    }

    private MetaExpression bodyAsXML(InputStream inputStream, MetaExpression status) throws IOException {
        String content = IOUtils.toString(inputStream);
        try {
            XmlNode xml = xmlNodeFactory.fromString(content);
            MetaExpression result = fromValue(xml.toString());
            result.storeMeta(xml);
            return result;
        } catch (RobotRuntimeException e) {
            throw this.throwBodyParseError(e, "XML", status);
        }
    }

    private OperationFailedException throwBodyParseError(Throwable e, String parseType, MetaExpression status) {
        LinkedHashMap<String, MetaExpression> statusValue = status.getValue();
        return new OperationFailedException("ParseResponseBodyAs" + parseType, e.getMessage() + " Response status code: " + statusValue.get("code"),
                "Fix by setting responseContentType=\"text/plain\"", e);
    }

    private MetaExpression bodyAsText(InputStream inputStream) throws IOException {
        String text = IOUtils.toString(inputStream);
        return fromValue(text);
    }

    private MetaExpression parseVersion(ProtocolVersion protocolVersion) {
        return fromValue(protocolVersion.toString());
    }

    private MetaExpression parseStatus(StatusLine statusLine) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>(2);
        result.put("code", fromValue(statusLine.getStatusCode()));
        result.put("phrase", fromValue(statusLine.getReasonPhrase()));
        return fromValue(result);
    }

    private MetaExpression parseHeaders(Header[] headers) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>(headers.length);

        for (Header header : headers) {
            if (!HEADER_COOKIES.equalsIgnoreCase(header.getName())) {
                result.put(header.getName(), fromValue(header.getValue()));
            }
        }

        return fromValue(result);
    }

    private MetaExpression parseCookies(Header[] headers) {
        LinkedHashMap<String, MetaExpression> result = new LinkedHashMap<>();
        for (Header cookieHeader : headers) {
            // Extract the data
            String cookieString = cookieHeader.getValue();
            String[] cookieParts = cookieString.split(";");
            String cookieDefinition = cookieParts[0];
            int cookieNameIndex = cookieDefinition.indexOf("=");
            String cookieName = cookieDefinition.substring(0, cookieNameIndex).trim();
            String cookieValue = cookieDefinition.substring(cookieNameIndex + 1).trim();

            // Put the cookie
            result.put(cookieName, parseCookie(cookieParts, cookieName, cookieValue));
        }
        return fromValue(result);
    }

    private MetaExpression parseCookie(String[] cookieParts, String cookieName, String cookieValue) {
        LinkedHashMap<String, MetaExpression> cookie = new LinkedHashMap<>();
        // name/value
        cookie.put("name", fromValue(cookieName));
        cookie.put("value", fromValue(cookieValue));

        // Other properties
        for (int i = 1; i < cookieParts.length; i++) {
            String cookiePartDef = cookieParts[i];
            int cookiePartDefIndex = cookiePartDef.indexOf("=");
            if (cookiePartDefIndex >= 0) {
                cookie.put(
                        cookiePartDef.substring(0, cookiePartDefIndex).trim().toLowerCase(),
                        fromValue(cookiePartDef.substring(cookiePartDefIndex + 1).trim())
                );
            } else {
                cookie.put(
                        cookiePartDef.toLowerCase().trim(),
                        TRUE
                );
            }
        }
        return fromValue(cookie);
    }
}
