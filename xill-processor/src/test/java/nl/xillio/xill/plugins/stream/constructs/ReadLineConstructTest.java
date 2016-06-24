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

import nl.xillio.xill.TestUtils;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.IOStream;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;


public class ReadLineConstructTest extends TestUtils {
    private final ReadLineConstruct construct = new ReadLineConstruct();

    @Test(dataProvider = "lineEndings")
    public void testNormalPath(String lineEnding) throws IOException {
        InputStream stream = IOUtils.toInputStream("This is my first line" + lineEnding + "This is my second");
        IOStream ioStream = new SimpleIOStream(stream, null);
        MetaExpression input = fromValue(ioStream);

        MetaExpression output = ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                input
        );

        assertEquals(output.getStringValue(), "This is my first line");
        assertEquals(IOUtils.toString(ioStream.getInputStream()), "This is my second");
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*NOT-EXIST.*")
    public void testBadCharset() {
        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                NULL,
                fromValue("NOT-EXIST")
        );
    }

    @Test(expectedExceptions = RobotRuntimeException.class, expectedExceptionsMessageRegExp = ".*end of the stream.*")
    public void testClosedStream() {
        InputStream stream = IOUtils.toInputStream("");
        IOStream ioStream = new SimpleIOStream(stream, null);
        MetaExpression input = fromValue(ioStream);

        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                input,
                NULL
        );
    }

    @DataProvider(name = "lineEndings")
    public Object[][] lineEndings() {
        return new Object[][]{
                new Object[]{
                        "\n"
                },
                new Object[]{
                        "\r"
                },
                new Object[]{
                        "\r\n"
                }
        };
    }

}