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
package nl.xillio.xill.plugins.string.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.InvalidUserInputException;
import nl.xillio.xill.api.io.SimpleIOStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class StreamConstruct extends Construct {
    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(this::process, new Argument("value", ATOMIC), new Argument("charset", fromValue("UTF-8")));
    }

    private MetaExpression process(MetaExpression value, MetaExpression charset) {
        String text = value.isNull() ? "" : value.getStringValue();

        try {
            return fromValue(new SimpleIOStream(
                    IOUtils.toInputStream(text, charset.getStringValue()),
                    "Streamed Text"
            ));
        } catch (IOException | IllegalArgumentException e) {
            throw new InvalidUserInputException(
                    "Unknown Character Set",
                    charset.getStringValue(),
                    "A valid character set such as 'UTF-8' or 'ISO-8859-1'",
                    "String.Stream(\"Hello World\", \"UTF-8\");"
            );
        }
    }
}
