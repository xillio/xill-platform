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

import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.stream.utils.StreamUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This construct will read text from a stream.
 * It can be limited by a limit parameter
 *
 * @author Thomas biesaart
 */
class GetTextConstruct extends Construct {

    @Override
    public ConstructProcessor prepareProcess(ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("stream", ATOMIC),
                new Argument("encoding", fromValue("UTF-8"), ATOMIC),
                new Argument("limit", fromValue(-1), ATOMIC)
        );
    }

    @SuppressWarnings("squid:UnusedPrivateMethod") // Sonar does not detect method references
    private MetaExpression process(MetaExpression stream, MetaExpression encoding, MetaExpression limitVar) {
        Charset charset = StreamUtils.getCharset(encoding);
        BufferedInputStream inputStream = StreamUtils.getInputStream(stream, "stream");
        long limit = limitVar.getNumberValue().longValue();

        String result = getText(inputStream, charset, limit);
        return fromValue(result);
    }

    private String getText(BufferedInputStream inputStream, Charset charset, long limit) {
        try {
            return StreamUtils.readText(inputStream, charset, limit);
        } catch (IOException e) {
            throw new OperationFailedException("read text", e.getMessage(), e);
        }
    }
}
