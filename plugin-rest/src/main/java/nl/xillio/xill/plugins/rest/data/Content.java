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

import me.biesaart.utils.Log;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.data.XmlNodeFactory;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;

import static nl.xillio.xill.api.components.ExpressionBuilderHelper.fromValue;

/**
 * Support class for storing the content:
 * either for request body
 * or for the response
 */
public class Content {

    private static final Logger LOGGER = Log.get();
    private HttpResponse fullResponse;
    private String content = "";
    private ContentType type = ContentType.TEXT_PLAIN;
    private MultipartBody multipartBody = null;
    private static final String ENCODING = "UTF-8";

    /**
     * Acquire content from Xill variable
     *
     * @param contentVar     request's body content
     * @param contentTypeVar request's body content type
     */
    public Content(final MetaExpression contentVar, final MetaExpression contentTypeVar) {
        if (contentVar.isNull()) {
            return;
        }

        if (contentTypeVar.isNull()) {
            if ((contentVar.getType() == ExpressionDataType.OBJECT) || (contentVar.getType() == ExpressionDataType.LIST)) {
                // JSON format
                this.content = contentVar.toString();
                this.type = ContentType.create(ContentType.APPLICATION_JSON.getMimeType(), ENCODING);
            } else if (contentVar.getMeta(XmlNode.class) != null) {
                // XML format
                XmlNode xmlNode = contentVar.getMeta(XmlNode.class);
                this.content = xmlNode.getXmlContent();
                this.type = ContentType.create(ContentType.APPLICATION_XML.getMimeType(), ENCODING);
            } else if (contentVar.getMeta(MultipartBody.class) != null) {
                // MultipartBody
                this.multipartBody = contentVar.getMeta(MultipartBody.class);
            } else {
                // Plain text content
                this.content = contentVar.getStringValue();
                this.type = ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), ENCODING);
            }
            this.type.withCharset("UTF-8");
        } else {
            this.type = ContentType.create(contentTypeVar.getStringValue(), ENCODING);
            this.content = contentVar.getStringValue();
        }
    }

    /**
     * Acquire content from string
     *
     * @param text content
     */
    public Content(final String text) {
        this.content = text;
        this.type = ContentType.TEXT_PLAIN;
    }

    /**
     * Acquire content from Apache Fluent response
     *
     * @param fullResponse the response
     */
    public Content(HttpResponse fullResponse) {

        this.fullResponse = fullResponse;

        if (fullResponse == null) {
            return;
        }

        try {
            this.content = IOUtils.toString(fullResponse.getEntity().getContent());
        } catch (IOException e) {
            LOGGER.error("Cannot read the content!", e);
        }
        this.type = ContentType.create(fullResponse.getEntity().getContentType().getValue());
    }

    /**
     * @return the content as a string
     */
    public String getContent() {
        if (this.multipartBody != null) {
            throw new RobotRuntimeException("Multipart can be used for request only!");
        }
        // else
        return this.content;
    }

    /**
     * @return the type of the content
     */
    public ContentType getType() {
        if (this.multipartBody != null) {
            throw new RobotRuntimeException("Multipart can be used for request only!");
        }
        // else
        return this.type;
    }

    /**
     * @return true if content is empty
     */
    public boolean isEmpty() {
        return (this.content.isEmpty() && (this.multipartBody == null));
    }

    /**
     * Create the Xill variable according to the type of content and fill it with proper content
     * It should be used for REST response only!
     *
     * @return new Xill variable (JSON-&gt;OBJECT type / XML-&gt;XmlNode / other-&gt;ATOMIC string)
     */
    public MetaExpression getMeta(JsonParser jsonParser, XmlNodeFactory xmlNodeFactory) {
        LinkedHashMap<String, MetaExpression> response = new LinkedHashMap<>();
        response.put("body", getMetaBody(jsonParser, xmlNodeFactory));

        if (fullResponse != null) {
            response.put("status", fromValue(fullResponse.getStatusLine().getStatusCode()));
            response.put("headers", fromValue(getMetaHeaders(fullResponse.getAllHeaders())));
        }

        return fromValue(response);
    }

    private LinkedHashMap<String, MetaExpression> getMetaHeaders(Header[] allHeaders) {
        LinkedHashMap<String, MetaExpression> headers = new LinkedHashMap<>();

        for (Header header : allHeaders) {
            headers.put(header.getName(), fromValue(header.getValue()));
        }

        return headers;
    }

    private MetaExpression getMetaBody(JsonParser jsonParser, XmlNodeFactory xmlNodeFactory) {
        // Only this is branch is covered with unit tests (FOR NOW) in accordance with a discussion with Thomas Biesaart.
        if (this.getType().getMimeType().contains("json")) {
            try {
                Object result = jsonParser.fromJson(this.getContent(), Object.class);
                return MetaExpression.parseObject(result); // Mockito does not provide mechanisms for testing static methods.
                // In order to test static methods, we would need to use PowerMockito.
            } catch (JsonException e) {
                LOGGER.error("Failed to parse response as json. Falling back.", e);
            }
        }

        if (this.getType().getMimeType().contains("xml")) {

            try {
                XmlNode xml = xmlNodeFactory.fromString(this.getContent());
                MetaExpression result = fromValue(xml.toString());
                result.storeMeta(xml);
                return result;
            } catch (Exception e) {
                LOGGER.error("Failed to parse XML. Falling back.", e);
            }
        }

        return fromValue(this.content);
    }

    /**
     * Set the single content or multipart body to the REST request
     *
     * @param request REST request
     */
    public void setToRequest(final Request request) {
        if (this.multipartBody == null) {
            request.bodyString(this.getContent(), this.getType());
        } else {
            this.multipartBody.setToRequest(request);
        }
    }
}
