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
package nl.xillio.xill.plugins.codec.encode.constructs;

import com.google.inject.Inject;
import nl.xillio.xill.api.components.MetaExpression;
import nl.xillio.xill.api.construct.Argument;
import nl.xillio.xill.api.construct.Construct;
import nl.xillio.xill.api.construct.ConstructContext;
import nl.xillio.xill.api.construct.ConstructProcessor;
import nl.xillio.xill.api.errors.OperationFailedException;
import nl.xillio.xill.plugins.codec.encode.services.EncoderService;

import java.io.UnsupportedEncodingException;

/**
 * Do URL encoding of the provided string.
 *
 * @author Zbynek Hochmann
 */
public class ToPercentConstruct extends Construct {

    private final EncoderService encoderService;

    @Inject
    public ToPercentConstruct(EncoderService encoderService) {
        this.encoderService = encoderService;
    }

    @Override
    public ConstructProcessor prepareProcess(final ConstructContext context) {
        return new ConstructProcessor(
                this::process,
                new Argument("string", ATOMIC),
                new Argument("xWwwForm", FALSE, ATOMIC));
    }

    MetaExpression process(final MetaExpression string, final MetaExpression xWwwFormVar) {
        try {
            return string.isNull() ? NULL : fromValue(encoderService.urlEncode(string.getStringValue(), !xWwwFormVar.isNull() && xWwwFormVar.getBooleanValue()));
        } catch (UnsupportedEncodingException e) {
            throw new OperationFailedException("URL encode the string", e.getMessage(), e);
        }
    }
}
