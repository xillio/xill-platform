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
package nl.xillio.xill.plugins.stream.constructs;

import me.biesaart.utils.StringUtils;
import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.components.MetaExpressionIterator;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class IterateConstructTest extends TestUtils {
    private final IterateConstruct construct = new IterateConstruct();

    @Test
    public void testConstructWithDefaultDelimiter() {
        List<String> lines = Arrays.asList("Hello", "World");
        InputStream input = IOUtils.toInputStream(StringUtils.join(lines, '\n'));

        MetaExpression result = process(new SimpleIOStream(input, "This is my description"), "");

        // String representation
        assertEquals(result.getStringValue(), "[Stream Iterator: This is my description]");

        // Data
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);
        List<String> results = new ArrayList<>();
        while (iterator.hasNext()) {
            MetaExpression item = iterator.next();
            results.add(item.getStringValue());
        }

        assertEquals(results, lines);
    }

    @Test
    public void testWithCustomDelimiter() {
        List<String> lines = Arrays.asList("Hello", "World");
        InputStream input = IOUtils.toInputStream(StringUtils.join(lines, " "));

        MetaExpression result = process(new SimpleIOStream(input, null), "\\s");

        // String representation
        assertEquals(result.getStringValue(), "[Stream Iterator]");

        // Data
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);
        List<String> results = new ArrayList<>();
        while (iterator.hasNext()) {
            MetaExpression item = iterator.next();
            results.add(item.getStringValue());
        }

        assertEquals(results, lines);
    }

    @Test
    public void testWithCustomDelimiterAndDescription() {
        List<String> lines = Arrays.asList("Hello", "World");
        InputStream input = IOUtils.toInputStream(StringUtils.join(lines, " "));

        MetaExpression result = process(new SimpleIOStream(input, "Custom Description"), "\\s");

        // String representation
        assertEquals(result.getStringValue(), "[Stream Iterator: Custom Description:\\s]");

        // Data
        MetaExpressionIterator iterator = result.getMeta(MetaExpressionIterator.class);
        List<String> results = new ArrayList<>();
        while (iterator.hasNext()) {
            MetaExpression item = iterator.next();
            results.add(item.getStringValue());
        }

        assertEquals(results, lines);
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*delimiter.*")
    public void testNullDelimiter() {
        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                NULL,
                NULL
        );
    }

    @Test
    public void testStreamIsRegisteredReleased() {
        List<String> lines = Arrays.asList("Hello", "World");
        InputStream input = IOUtils.toInputStream(StringUtils.join(lines, " "));
        IOStream inputStream = spy(new SimpleIOStream(input, "Irrelevant Description"));
        MetaExpression metaExpressionStream = fromValue(inputStream);

        MetaExpression iterateInstance = process(construct, metaExpressionStream, fromValue("\\s"));
        MetaExpressionIterator iterator = iterateInstance.getMeta(MetaExpressionIterator.class);

        for(String word : lines) {
            assertEquals(iterator.next().getStringValue(), word);
        }

        verify(inputStream, never()).close();
        iterateInstance.close();
        verify(inputStream, times(1)).close();
    }

    private MetaExpression process(IOStream stream, String delim) {
        return ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                fromValue(stream),
                fromValue(delim)
        );
    }
}