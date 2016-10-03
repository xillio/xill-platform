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
package nl.xillio.xill.services.json;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import nl.xillio.xill.api.components.MetaExpression;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

/**
 * This implementation of the JsonParser uses Jackson.
 *
 * @author Thomas Biesaart
 */
public class JacksonParser implements PrettyJsonParser {

    private static class PrettyPrinter extends DefaultPrettyPrinter {
        public static final PrettyPrinter instance = new PrettyPrinter();

        public PrettyPrinter() {
            _arrayIndenter = new DefaultIndenter();
        }
    }

    private static class Factory extends JsonFactory {
        @Override
        protected JsonGenerator _createGenerator(Writer out, IOContext ioContext) throws IOException {
            return super._createGenerator(out, ioContext).setPrettyPrinter(PrettyPrinter.instance);
        }
    }

    private final ObjectMapper mapper;

    public JacksonParser(boolean pretty) {
        if (pretty) {
            mapper = new ObjectMapper(new Factory());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            mapper = new ObjectMapper();
        }
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    }

    @Override
    @SuppressWarnings("squid:S1166") // Ignore StackOverflowError not being handled since it is too large due to the circular reference.
    public String toJson(Object object) throws JsonException {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to parse json: " + e.getMessage(), e);
        } catch (StackOverflowError e) {
            throw new JsonException("A stack overflow error occurred while parsing the JSON, this is likely due a circular reference.");
        }
    }

    @Override
    public String toJson(MetaExpression metaExpression) throws JsonException {
        return toJson((Object) MetaExpression.extractValue(metaExpression));
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) throws JsonException {
        ObjectMapper newMapper = new ObjectMapper();
        try {
            return newMapper.readValue(json, type);
        } catch (IOException e) {
            throw new JsonException("Failed to parse json: " + e.getMessage(), e);
        }
    }
}
