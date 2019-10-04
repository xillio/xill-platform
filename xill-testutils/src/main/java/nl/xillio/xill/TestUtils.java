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
package nl.xillio.xill;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import nl.xillio.events.EventHost;
import nl.xillio.xill.api.Debugger;
import nl.xillio.xill.api.components.ExpressionBuilderHelper;
import nl.xillio.xill.api.components.ExpressionDataType;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.RobotID;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.data.Date;
import nl.xillio.xill.api.data.MetadataExpression;
import nl.xillio.xill.services.files.FileResolver;
import nl.xillio.xill.services.json.JacksonParser;
import nl.xillio.xill.services.json.JsonException;
import nl.xillio.xill.services.json.JsonParser;
import nl.xillio.xill.services.json.PrettyJsonParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This class represents various utilities you could need during testing of a xill plugin
 */
public class TestUtils extends ExpressionBuilderHelper {
    protected static final FileResolver CONSTRUCT_FILE_RESOLVER;
    private static final JsonParser jsonParser = new JacksonParser(true);

    static {
        CONSTRUCT_FILE_RESOLVER = mock(FileResolver.class);
        Guice.createInjector(new TestModule());
    }

    public static MetaExpression makeMeta(MetadataExpression date) {
        MetaExpression result = fromValue(date.toString());
        result.storeMeta(date);

        return result;
    }

    public static MetaExpression parseJson(String json) throws JsonException {
        return parseObject(jsonParser.fromJson(json, Object.class));
    }

    static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(FileResolver.class).toInstance(CONSTRUCT_FILE_RESOLVER);
            bind(JsonParser.class).toInstance(new JacksonParser(false));
            bind(PrettyJsonParser.class).toInstance(new JacksonParser(true));
            requestStaticInjection(Construct.class);
            requestStaticInjection(MetaExpression.class);
        }
    }

    protected static void setFileResolverReturnValue(Path file) {
        doReturn(file.toFile()).when(CONSTRUCT_FILE_RESOLVER).buildFile(any(), anyString());
        doReturn(file).when(CONSTRUCT_FILE_RESOLVER).buildPath(any(), any());
    }


    /**
     * Mock a {@link MetaExpression} of a specific type
     *
     * @param type the type of the {@link MetaExpression}
     * @return the {@link MetaExpression}
     */
    protected static MetaExpression mockExpression(final ExpressionDataType type) {
        MetaExpression expression = mock(MetaExpression.class);
        when(expression.getType()).thenReturn(type);
        return expression;
    }

    /**
     * Mock a {@link MetaExpression} that holds a certain value
     *
     * @param type        the type
     * @param boolValue   the result of {@link MetaExpression#getBooleanValue()}
     * @param numberValue the result of {@link MetaExpression#getStringValue()}
     * @param stringValue the result of {@link MetaExpression#getNumberValue()}
     * @return the expression
     */
    protected static MetaExpression mockExpression(final ExpressionDataType type, final boolean boolValue, final double numberValue, final String stringValue) {
        MetaExpression expression = mockExpression(type);

        when(expression.getBooleanValue()).thenReturn(boolValue);
        when(expression.getNumberValue()).thenReturn(numberValue);
        when(expression.getStringValue()).thenReturn(stringValue);
        return expression;
    }

    protected ConstructContext context() {
        return context(mock(Construct.class));
    }

    protected ConstructContext context(Construct construct) {
        return new ConstructContext(Paths.get("."), RobotID.dummyRobot(), RobotID.dummyRobot(), construct, mock(Debugger.class, RETURNS_DEEP_STUBS), UUID.randomUUID(), new EventHost<>(), new EventHost<>(), null);
    }

    protected MetaExpression process(Construct construct, MetaExpression... arguments) {
        try (ConstructProcessor processor = construct.prepareProcess(context(construct))) {
            return ConstructProcessor.process(
                    processor,
                    arguments
            );
        }
    }

    protected MetaExpression createMap(Object... input) {
        if (input.length % 2 != 0) {
            throw new IllegalArgumentException("Input must be an even amount of elements");
        }

        LinkedHashMap<String, MetaExpression> map = new LinkedHashMap<>();
        for (int i = 0; i < input.length; i += 2) {
            map.put(input[i].toString(), parseValue(input[i + 1]));
        }

        return fromValue(map);
    }

    protected MetaExpression createList(Object... input) {
        List<MetaExpression> list = Arrays.stream(input)
                .map(this::parseValue)
                .collect(Collectors.toList());
        return fromValue(list);
    }

    private MetaExpression parseValue(Object input) {
        if (input instanceof MetaExpression) {
            return (MetaExpression) input;
        }
        return MetaExpression.parseObject(input);
    }
}
