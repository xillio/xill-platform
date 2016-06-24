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

import me.biesaart.utils.IOUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Test class for the Content class.
 */
public class ContentTest {

    /**
     * Tests the getMeta() method of the Content class.
     *
     * @throws IOException
     */
    @Test
    public void testGetMeta() throws IOException {

        HttpResponse fullResponse = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(fullResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(IOUtils.toInputStream("XILLIDE"));
        Header header = new BasicHeader("type", "application/json");
        when(httpEntity.getContentType()).thenReturn(header);

        Content content = new Content(fullResponse);
        Header[] headers = new Header[]{new BasicHeader("type", "XILLSERVER")};
        when(fullResponse.getStatusLine().getStatusCode()).thenReturn(200);
        when(fullResponse.getAllHeaders()).thenReturn(headers);

        // Call to getMeta()
        MetaExpression result = content.getMeta(new JacksonParser(false), null);
        assertEquals(result.toString(), "{\"body\":\"XILLIDE\",\"status\":200,\"headers\":{\"type\":\"XILLSERVER\"}}");
    }

    /**
     * Test the conditional branch "json" of the private method getMetaBody(). This method, i.e. getMetaBody(), is
     * called by the getMeta() method in the Content class.
     *
     * @throws IOException
     * @throws JsonException
     */
    @Test
    public void testPartialGetMetaBody() throws IOException, JsonException {

        HttpResponse fullResponse = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
        when(fullResponse.getEntity().getContent()).thenReturn(IOUtils.toInputStream("application/json"));
        Header header = new BasicHeader("type", "application/json");
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContentType()).thenReturn(header);
        JsonParser jsonParser = new JacksonParser(false);
        Object o = jsonParser.fromJson("{\"body\":\"XILLIDE\"}", Object.class);
        assertEquals("{\"body\":\"XILLIDE\"}", MetaExpression.parseObject(o).toString());
    }
}