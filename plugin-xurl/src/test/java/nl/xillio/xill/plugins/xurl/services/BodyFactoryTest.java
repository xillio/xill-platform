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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.data.XmlNode;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.SimpleIOStream;
import nl.xillio.xill.plugins.xurl.data.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BodyFactoryTest extends TestUtils {
    private final BodyFactory bodyFactory = new BodyFactory(CONSTRUCT_FILE_RESOLVER);

    @Test
    public void testJsonBody() {
        MetaExpression json = fromValue(Arrays.asList(fromValue("Hello"), fromValue("World")));
        String body = testBody(json);
        assertEquals(body, json.toString());
    }

    @Test
    public void testStringBody() {
        MetaExpression input = fromValue("Hello World. This is plain text content");
        String data = testBody(input);
        assertEquals(data, input.getStringValue());
    }

    @Test
    public void testXMLBody() {
        String xmlSource = "<note><message>Hello World</message><note>";
        XmlNode xmlNode = mock(XmlNode.class);
        when(xmlNode.getXmlContent()).thenReturn(xmlSource);
        MetaExpression item = fromValue("");
        item.storeMeta(xmlNode);

        String data = testBody(item);
        assertEquals(data, xmlSource);
    }

    @Test
    public void testStreamBody() throws IOException {
        String text = "This is a the input value";
        InputStream inputStream = IOUtils.toInputStream(text);
        MetaExpression input = fromValue(new SimpleIOStream(inputStream, null));

        InputStream result = testBodyStream(input);

        assertEquals(IOUtils.toString(result), text);
    }

    private String testBody(MetaExpression expression) {
        Request request = mock(Request.class);
        ArgumentCaptor<String> stringBody = forClass(String.class);

        bodyFactory.applyBody(request, expression, new Options(), context());
        verify(request).bodyString(stringBody.capture(), any());
        return stringBody.getValue();
    }

    private InputStream testBodyStream(MetaExpression expression) {
        Request request = mock(Request.class);
        ArgumentCaptor<InputStream> inputStreamBody = forClass(InputStream.class);

        bodyFactory.applyBody(request, expression, new Options(), context());
        verify(request).bodyStream(inputStreamBody.capture(), any());
        return inputStreamBody.getValue();
    }

    /**
     * @return Testing mulipart bodies
     */
    @DataProvider(name = "multipartBodies")
    public Object[][] multipartBodyProvider() {
        return new Object[][]{
                {
                        map(
                                "textInput", map(
                                        "content", "This is my text value",
                                        "type", "text"
                                ),
                                "streamInput", map(
                                        "content", fromValue(new SimpleIOStream(IOUtils.toInputStream("Hello Stream"), "")),
                                        "type", "stream"
                                )
                        )
                },
                {
                        list(
                                map(
                                        "name", "textInput",
                                        "content", "This is my text value",
                                        "type", "text"
                                )
                                , map(
                                        "name", "streamInput",
                                        "content", fromValue(new SimpleIOStream(IOUtils.toInputStream("Hello Stream"), "")),
                                        "type", "stream"
                                )
                        )
                }
        };
    }

    @Test(dataProvider = "multipartBodies")
    public void testMultiPart(MetaExpression body) throws IOException {
        // Input
        Options options = new Options();
        options.setMultipart(true);

        Request request = mock(Request.class);
        ArgumentCaptor<HttpEntity> bodyCaptor = forClass(HttpEntity.class);


        bodyFactory.applyBody(request, body, options, context());
        verify(request).body(bodyCaptor.capture());

        HttpEntity entity = bodyCaptor.getValue();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entity.writeTo(outputStream);

        String content = outputStream.toString();

        // Text
        assertTrue(content.contains("This is my text value"));
        assertTrue(content.contains("Content-Disposition: form-data; name=\"textInput\""));

        // Stream
        assertTrue(content.contains("Hello Stream"));
        assertTrue(content.contains("Content-Disposition: form-data; name=\"streamInput\""));
    }

    /**
     * @return Testing mulipart bodies with wrongly named fields
     */
    @DataProvider(name = "multipartBodiesWrongField")
    public Object[][] multipartBodyWrongFieldProvider() {
        return new Object[][]{
                {
                        map(
                                "textInput", map(
                                        "name", "This is wrong",
                                        "content", "This is my text value",
                                        "type", "text"
                                ),
                                "streamInput", map(
                                        "content", fromValue(new SimpleIOStream(IOUtils.toInputStream("Hello Stream"), "")),
                                        "type", "stream"
                                )
                        )
                },
                {
                        list(
                                map(
                                        "name", "textInput",
                                        "content", "This is my text value",
                                        "type", "text",
                                        "error", "This is wrong"
                                )
                                , map(
                                        "name", "streamInput",
                                        "content", fromValue(new SimpleIOStream(IOUtils.toInputStream("Hello Stream"), "")),
                                        "type", "stream"
                                )
                        )
                }
        };
    }

    /**
     * Test that passing a multipart body part with a wrongly named field results in an error
     * @param body The body to test with
     */
    @Test(dataProvider = "multipartBodiesWrongField", expectedExceptions = RobotRuntimeException.class)
    public void testMultipartWrongField (MetaExpression body) {
        // Input
        Options options = new Options();
        options.setMultipart(true);

        Request request = mock(Request.class);
        ArgumentCaptor<HttpEntity> bodyCaptor = forClass(HttpEntity.class);

        // Run
        bodyFactory.applyBody(request, body, options, context());
    }

}
