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
package nl.xillio.xill.plugins.system.constructs;

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.RobotRuntimeException;
import nl.xillio.xill.api.io.SimpleIOStream;

import java.io.IOException;
import java.io.InputStream;

public class OpenResourceConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(path -> process(context, path), new Argument("path", ATOMIC));
    }

    static MetaExpression process(ConstructContext context, MetaExpression path) {
        InputStream stream;
        try {
            stream = context.getResourceLoader().getResourceAsStream(path.getStringValue());
        } catch (IOException e) {
            throw new RobotRuntimeException("Could not open stream to resource.", e);
        }
        return stream != null ? fromValue(new SimpleIOStream(stream, null)) : NULL;
    }
}
