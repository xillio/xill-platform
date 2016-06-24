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
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;


public class GetTextConstructTest extends TestUtils {
    private GetTextConstruct construct = new GetTextConstruct();

    @Test
    public void testMinimalInput() {
        String text = "Sup Everyone?? I am writing unit tests";
        IOStream stream = new SimpleIOStream(IOUtils.toInputStream(text), null);
        MetaExpression input = fromValue(stream);


        MetaExpression result = ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                input
        );

        assertEquals(result.getStringValue(), text);
    }

    @Test(expectedExceptions = RobotRuntimeException.class)
    public void testReadTextClosedStream() throws IOException {
        IOStream stream = new SimpleIOStream(IOUtils.toInputStream(""), null);
        stream.getInputStream().close();
        MetaExpression input = fromValue(stream);


        ConstructProcessor.process(
                construct.prepareProcess(context(construct)),
                input
        );
    }
}