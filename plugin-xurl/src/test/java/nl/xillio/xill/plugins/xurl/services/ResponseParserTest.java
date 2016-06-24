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

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.data.XmlNodeFactory;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.plugins.xurl.data.Options;
import nl.xillio.xill.services.json.JacksonParser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class ResponseParserTest extends TestUtils {
    private final XmlNodeFactory nodeFactory = mock(XmlNodeFactory.class);
    private final ResponseParser responseParser = new ResponseParser(new JacksonParser(true), nodeFactory);

    @Test
    public void testParseFullResponseWithTextBody() throws IOException {
        String bodyText = "This is the body of my response";
        HttpResponse httpResponse = textResponse(bodyText, new BasicHeader("x-header", "nice"));

        MetaExpression result = responseParser.build(mock(Response.class), httpResponse, new Options());

        assertEquals(result.toString(), "{\"status\":{\"code\":200,\"phrase\":\"OK\"},\"headers\":{\"x-header\":\"nice\"},\"version\":\"HTTP/1.0\",\"body\":\"This is the body of my response\"}");
    }

    @Test
    public void testParseJSONBody() throws IOException {
        String bodyText = "[1,2,3,4,5]";
        HttpResponse httpResponse = jsonResponse(bodyText);

        MetaExpression result = responseParser.parseBody(httpResponse, null, this.mockResponseStatus());

        assertEquals(result.getType(), LIST);
        assertEquals(result.toString(), "[1,2,3,4,5]");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*json.*responseContentType.*")
    public void testJsonBodyInvalid() throws IOException {
        String bodyText = "[1,2,3,4,5";
        HttpResponse httpResponse = jsonResponse(bodyText);

        responseParser.parseBody(httpResponse, null, this.mockResponseStatus());
    }

    private MetaExpression mockResponseStatus()
    {
        LinkedHashMap<String, MetaExpression> responseStatusValue = new LinkedHashMap<String, MetaExpression>(2);
        responseStatusValue.put("code", fromValue(515));

        return fromValue(responseStatusValue);
    }

    @Test
    public void testParseXMLBody() throws IOException {
        verify(nodeFactory, never()).fromString(anyString());

        String bodyText = "<node></node>";
        XmlNode node = mock(XmlNode.class);
        when(node.getXmlContent()).thenReturn(bodyText);
        when(nodeFactory.fromString(anyString())).thenReturn(node);

        HttpResponse httpResponse = xmlResponse(bodyText);

        MetaExpression result = responseParser.parseBody(httpResponse, null, this.mockResponseStatus());

        assertEquals(result.getType(), ExpressionDataType.ATOMIC);
        assertTrue(result.hasMeta(XmlNode.class));
    }

    @Test
    public void testStreamBody() throws IOException {
        String bodyText = "This is the body of this stream type";
        InputStream stream = IOUtils.toInputStream(bodyText);
        HttpResponse response = response(stream, ContentType.DEFAULT_BINARY);

        MetaExpression result = responseParser.parseBody(response, ContentType.DEFAULT_BINARY, null);

        InputStream resultStream = result.getBinaryValue().getInputStream();

        assertEquals(IOUtils.toString(resultStream), bodyText);
    }

    private HttpResponse textResponse(String bodyText, Header... headers) {
        return response(IOUtils.toInputStream(bodyText), ContentType.TEXT_PLAIN, headers);
    }

    private HttpResponse jsonResponse(String bodyText, Header... headers) {
        return response(IOUtils.toInputStream(bodyText), ContentType.APPLICATION_JSON, headers);
    }

    private HttpResponse xmlResponse(String bodyText, Header... headers) {
        return response(IOUtils.toInputStream(bodyText), ContentType.APPLICATION_ATOM_XML, headers);
    }

    private HttpResponse response(InputStream body, ContentType contentType, Header... headers) {
        ProtocolVersion version = new ProtocolVersion("HTTP", 1, 0);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(version, 200, "OK"));
        when(response.getAllHeaders()).thenReturn(headers);
        when(response.getProtocolVersion()).thenReturn(version);

        if (body != null) {
            HttpEntity entity = new InputStreamEntity(body, contentType);
            when(response.getEntity()).thenReturn(entity);
        }

        return response;
    }
}